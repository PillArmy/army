/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.spring.ai.vectorstore;

import io.army.criteria.*;
import io.army.criteria.impl.MySQLs;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.dialect.Database;
import io.army.mapping.optional.JsonPathType;
import io.army.meta.FieldMeta;
import io.army.meta.IndexMeta;
import io.army.meta.ServerMeta;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;

import java.util.*;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.AS;

/// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
public final class ArmyVectorStore extends AbstractObservationVectorStore {


    private final NullMode nullMode;

    private final LiteralMode literalMode;

    private final int maxDocumentBatchSize;

    private final int batchDeleteThreshold;

    private final SyncSessionContext sessionContext;

    private final DistanceType distanceType;

    private final FilterExpressionConverter converter;


    private ArmyVectorStore(Builder builder) {
        super(builder);

        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;
        this.maxDocumentBatchSize = builder.maxDocumentBatchSize;
        this.batchDeleteThreshold = builder.batchDeleteThreshold;

        this.sessionContext = builder.sessionContext;
        this.distanceType = builder.distanceType;


        switch (builder.sessionContext.sessionFactory().dialectDatabase()) {
            case PostgreSQL:
                this.converter = new PgVectorFilterExpressionConverter();
                break;
            case MySQL:
            default: {
                String m = String.format("%s is unsupported", builder.sessionContext.sessionFactory().dialectDatabase());
                throw new IllegalArgumentException(m);
            }
        }
    }


    @Override
    public void doAdd(final List<Document> documents) {
        final List<float[]> embeddingList;
        embeddingList = this.embeddingModel.embed(documents, EmbeddingOptions.builder().build(), this.batchingStrategy);

        final Function<SyncSession, Void> function;
        function = session -> {
            final Database database = session.dialectDatabase();

            final int documentCount = documents.size(), maxBatchSize = this.maxDocumentBatchSize;
            final List<SpringAiVectorStore> rowList = new ArrayList<>(Math.min(documentCount, maxBatchSize));
            SpringAiVectorStore o;
            Document document;
            for (int offset = 0, batchEnd; offset < documentCount; offset += maxBatchSize) {
                batchEnd = Math.min(documentCount, offset + maxBatchSize);

                for (int i = offset; i < batchEnd; i++) {
                    document = documents.get(i);

                    o = new SpringAiVectorStore()
                            .setId(document.getId())
                            .setContent(document.getText())
                            .setMetadata(document.getMetadata())
                            .setEmbedding(embeddingList.get(i));

                    rowList.add(o);
                } // inner loop

                switch (database) {
                    case PostgreSQL:
                        session.update(postgreInsertOrUpdateStmt(rowList));
                        break;
                    case MySQL:
                        session.update(mysqlInsertOrUpdateStmt(rowList));
                        break;
                    default:
                        throw new IllegalStateException(String.format("%s is supported", database));
                } //  switch (database)

                rowList.clear(); // clear

            } // loop

            return null;
        };

        final String sessionName = getClass().getName() + '.' + "add";
        this.sessionContext.execute(sessionName, false, function);
    }


    @Override
    public void doDelete(final List<String> idList) {
        final int idCount = idList.size();
        final DeleteStatement stmt;
        if (idCount < this.batchDeleteThreshold) {
            stmt = SQLs.singleDelete()
                    .deleteFrom(SpringAiVectorStore_.T, AS, "t")
                    .where(SpringAiVectorStore_.id.in(SQLs::rowParam, idList))
                    .asDelete();
        } else {
            final List<Map<String, String>> paramList = new ArrayList<>(idCount);
            for (String id : idList) {
                paramList.add(Map.of(SpringAiVectorStore_.ID, id));
            }
            stmt = SQLs.batchSingleDelete()
                    .deleteFrom(SpringAiVectorStore_.T, AS, "t")
                    .where(SpringAiVectorStore_.id.spaceEqual(SQLs::namedParam))
                    .asDelete()
                    .namedParamList(paramList);
        }

        final Function<SyncSession, Void> function;
        function = session -> {

            if (stmt instanceof BatchDelete) {
                session.batchUpdate((BatchDelete) stmt);
            } else {
                session.update((Delete) stmt);
            }
            return null;
        };

        final String sessionName = getClass().getName() + '.' + "deleteById";
        this.sessionContext.execute(sessionName, false, function);
    }


    @Override
    protected void doDelete(Filter.Expression filterExpression) {
        final String jsonPath;
        jsonPath = this.converter.convertExpression(filterExpression);

        final Delete stmt;
        stmt = SQLs.singleDelete()
                .deleteFrom(SpringAiVectorStore_.T, AS, "t")
                .where(SpringAiVectorStore_.metadata.space(Postgres.AT_AT, SQLs.literal(JsonPathType.INSTANCE, jsonPath)))
                .asDelete();

        final Function<SyncSession, Void> function;
        function = session -> {
            session.update(stmt);
            return null;
        };

        final String sessionName = getClass().getName() + '.' + "deleteByExp";
        this.sessionContext.execute(sessionName, false, function);
    }

    @Override
    public List<Document> doSimilaritySearch(final SearchRequest request) {
        final Filter.Expression expression = request.getFilterExpression();
        final String jsonPath;
        if (expression == null) {
            jsonPath = null;
        } else {
            jsonPath = this.converter.convertExpression(expression);
        }

        final double distance = 1 - request.getSimilarityThreshold();

        final float[] embedding;
        embedding = this.embeddingModel.embed(request.getQuery());

        final SQLs.DualOperator operator;
        operator = switch (this.distanceType) {
            case L2_DISTANCE -> SQLs.L2_DISTANCE;
            case COSINE_DISTANCE -> SQLs.COSINE_DISTANCE;
            case NEG_DOT -> SQLs.NEG_DOT;
            default -> throw _Exceptions.unexpectedEnum(this.distanceType);
        };

        final String distanceLabel = "distance";

        final Select stmt;
        stmt = SQLs.query()
                .selects(s -> {
                    s.selection(SpringAiVectorStore_.id, SpringAiVectorStore_.content, SpringAiVectorStore_.metadata)
                            .selection(SpringAiVectorStore_.embedding);
                    if (operator == SQLs.NEG_DOT) {
                        s.selection(SQLs.LITERAL_1.plus(SpringAiVectorStore_.embedding.space(operator, embedding)).as(distanceLabel));
                    } else {
                        s.selection(SpringAiVectorStore_.embedding.space(operator, embedding).as(distanceLabel));
                    }
                })
                .from(SpringAiVectorStore_.T, AS, "t")
                .where(wb -> {
                    if (operator == SQLs.NEG_DOT) {
                        wb.accept(SQLs.LITERAL_1.plus(SpringAiVectorStore_.embedding.space(operator, embedding)).less(distance));
                    } else {
                        wb.accept(SpringAiVectorStore_.embedding.space(operator, embedding).less(distance));
                    }
                    if (jsonPath != null) {
                        wb.accept(SpringAiVectorStore_.metadata.space(Postgres.AT_AT, SQLs.literal(JsonPathType.INSTANCE, jsonPath)));
                    }
                })
                .orderBy(SQLs.refSelection(distanceLabel))
                .limit(request.getTopK())
                .asQuery();

        final Function<CurrentRecord, Document> rowFunc;
        rowFunc = row -> {
            final double distanceValue;
            distanceValue = row.getNonNull(3, Double.class);

            final Map<String, Object> metadata;
            metadata = row.getNonNullMap(2, String.class, Object.class);
            metadata.put(DocumentMetadata.DISTANCE.value(), distanceValue);

            return Document.builder()
                    .id(row.getNonNull(0, SpringAiVectorStore_.id.javaType()).toString())
                    .text(row.get(1, String.class))
                    .metadata(metadata)
                    .score(1.0 - distanceValue)
                    .build();
        };

        final Function<SyncSession, List<Document>> function;
        function = session -> session.queryRecordList(stmt, rowFunc);

        final String sessionName = getClass().getName() + '.' + "doSimilaritySearch";
        return this.sessionContext.execute(sessionName, true, function);
    }


    @Override
    public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {
        final ServerMeta serverMeta = this.sessionContext.sessionFactory().serverMeta();
        final String nameSpace;
        switch (serverMeta.serverDatabase()) {
            case PostgreSQL:
                nameSpace = Objects.requireNonNull(serverMeta.schema());
                break;
            case MySQL: {
                String value;
                value = serverMeta.catalog();
                if (_StringUtils.hasText(value)) {
                    nameSpace = value;
                } else if (_StringUtils.hasText(value = serverMeta.schema())) {
                    nameSpace = value;
                } else {
                    throw new IllegalStateException("bug");
                }
            }
            break;
            default:
                throw _Exceptions.unexpectedEnum(serverMeta.serverDatabase());
        }

        final VectorStoreSimilarityMetric metric;
        metric = switch (this.distanceType) {
            case L2_DISTANCE -> VectorStoreSimilarityMetric.EUCLIDEAN;
            case COSINE_DISTANCE -> VectorStoreSimilarityMetric.COSINE;
            case NEG_DOT -> VectorStoreSimilarityMetric.DOT;
            default -> throw _Exceptions.unexpectedEnum(this.distanceType);
        };

        return VectorStoreObservationContext.builder(VectorStoreProvider.PG_VECTOR.value(), operationName)
                .collectionName(SpringAiVectorStore_.T.tableName())
                .dimensions(SpringAiVectorStore_.embedding.precision())
                .namespace(nameSpace)
                .similarityMetric(metric.value());
    }


    private Insert postgreInsertOrUpdateStmt(final List<SpringAiVectorStore> rowList) {
        return Postgres.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(SpringAiVectorStore_.T).as("t")
                .values(rowList)
                .onConflict().parens(s -> s.space(SpringAiVectorStore_.id))
                .doUpdate()
                .set(SpringAiVectorStore_.content, Postgres.excluded(SpringAiVectorStore_.content))
                .set(SpringAiVectorStore_.metadata, Postgres.excluded(SpringAiVectorStore_.metadata))
                .set(SpringAiVectorStore_.embedding, Postgres.excluded(SpringAiVectorStore_.embedding))
                .asInsert();
    }

    private Insert mysqlInsertOrUpdateStmt(final List<SpringAiVectorStore> rowList) {
        final String rowAlias = "r";
        return MySQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(SpringAiVectorStore_.T)
                .values(rowList)
                .as(rowAlias)
                .onDuplicateKey()
                .update(SpringAiVectorStore_.content, SQLs.field(rowAlias, SpringAiVectorStore_.content))
                .comma(SpringAiVectorStore_.metadata, SQLs.field(rowAlias, SpringAiVectorStore_.metadata))
                .comma(SpringAiVectorStore_.embedding, SQLs.field(rowAlias, SpringAiVectorStore_.embedding))
                .asInsert();
    }


    public static Builder builder(EmbeddingModel embeddingModel, SyncSessionContext sessionContext) {
        return new Builder(embeddingModel, sessionContext);
    }

    public enum DistanceType {


        /// <-> - L2 distance
        ///
        /// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
        L2_DISTANCE("vector_l2_ops"),

        ///  <=> - Cosine distance
        ///
        /// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
        COSINE_DISTANCE("vector_cosine_ops"),

        /// (negative) inner product
        NEG_DOT("vector_ip_ops");

        public final String opclass;

        DistanceType(String opclass) {
            this.opclass = opclass;
        }


    }

    public static final class Builder extends AbstractVectorStoreBuilder<Builder> {

        private final SyncSessionContext sessionContext;

        private NullMode nullMode = NullMode.DEFAULT;

        private LiteralMode literalMode = LiteralMode.DEFAULT;

        private int maxDocumentBatchSize = 1000;

        private int batchDeleteThreshold = 20;


        private DistanceType distanceType;

        private Builder(EmbeddingModel embeddingModel, SyncSessionContext sessionContext) {
            super(embeddingModel);
            this.sessionContext = sessionContext;
        }


        public Builder maxDocumentBatchSize(int maxDocumentBatchSize) {
            this.maxDocumentBatchSize = maxDocumentBatchSize;
            return this;
        }

        public Builder batchDeleteThreshold(int batchDeleteThreshold) {
            this.batchDeleteThreshold = batchDeleteThreshold;
            return this;
        }

        public Builder nullMode(NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        public Builder literalMode(LiteralMode literalMode) {
            this.literalMode = literalMode;
            return this;
        }

        public Builder distanceType(DistanceType distanceType) {
            this.distanceType = distanceType;
            return this;
        }


        @Override
        public VectorStore build() {

            if (this.distanceType == null) {
                this.distanceType = findDistanceType();
            }

            return new ArmyVectorStore(this);
        }

        private static DistanceType findDistanceType() {
            List<FieldMeta<SpringAiVectorStore>> fieldList;
            String opclass;

            DistanceType distanceType = null;
            topLoop:
            for (IndexMeta<SpringAiVectorStore> index : SpringAiVectorStore_.T.indexList()) {
                fieldList = index.fieldList();
                if (fieldList.size() != 1) {
                    continue;
                }
                if (!SpringAiVectorStore_.embedding.equals(fieldList.getFirst())) {
                    continue;
                }
                opclass = index.type();
                if (!_StringUtils.hasText(opclass)) {
                    break;
                }
                opclass = opclass.toLowerCase(Locale.ROOT);
                for (DistanceType v : DistanceType.values()) {
                    if (v.opclass.equals(opclass)) {
                        distanceType = v;
                        break topLoop;
                    }
                }
            }

            if (distanceType == null) {
                String m = String.format("%s no opclass and don't config %s", SpringAiVectorStore_.embedding,
                        DistanceType.class.getName());
                throw new IllegalArgumentException(m);
            }

            return distanceType;
        }


    } //

}
