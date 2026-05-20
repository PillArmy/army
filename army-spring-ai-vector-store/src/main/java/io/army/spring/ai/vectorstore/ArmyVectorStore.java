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
import io.army.meta.*;
import io.army.pojo.ObjectAccessorFactory;
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
import java.util.function.Supplier;

import static io.army.criteria.impl.SQLs.AS;

/// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
public final class ArmyVectorStore<T extends SpringAiVectorStore> extends AbstractObservationVectorStore {


    private final NullMode nullMode;

    private final LiteralMode literalMode;

    private final int maxDocumentBatchSize;

    private final int batchDeleteThreshold;

    private final SyncSessionContext sessionContext;

    private final DistanceType distanceType;

    private final String model;

    private final SimpleTableMeta<T> tableMeta;

    private final PrimaryFieldMeta<T> id;

    private final FieldMeta<T> content;

    private final FieldMeta<T> metadata;

    private final FieldMeta<T> embedding;

    private final FieldMeta<T> documentId;

    private final Supplier<T> constructor;

    private final FilterExpressionConverter converter;


    private ArmyVectorStore(Builder<T> builder) {
        super(builder);

        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;
        this.maxDocumentBatchSize = builder.maxDocumentBatchSize;
        this.batchDeleteThreshold = builder.batchDeleteThreshold;

        this.sessionContext = builder.sessionContext;
        this.distanceType = builder.distanceType;
        this.model = builder.model;

        this.tableMeta = builder.tableMeta;

        this.id = builder.tableMeta.id();

        this.content = this.tableMeta.field("content");
        this.metadata = this.tableMeta.field("metadata");
        this.embedding = this.tableMeta.field("embedding");
        this.documentId = this.tableMeta.field("documentId");

        this.constructor = constructorFunc(this.tableMeta);

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
        embeddingList = this.embeddingModel.embed(documents, embeddingOptions(), this.batchingStrategy);

        final Function<SyncSession, Void> function;
        function = session -> {
            final Database database = session.dialectDatabase();

            final int documentCount = documents.size(), maxBatchSize = this.maxDocumentBatchSize;
            final List<T> rowList = new ArrayList<>(Math.min(documentCount, maxBatchSize));
            T o;
            Document document;
            for (int offset = 0, batchEnd; offset < documentCount; offset += maxBatchSize) {
                batchEnd = Math.min(documentCount, offset + maxBatchSize);

                for (int i = offset; i < batchEnd; i++) {
                    document = documents.get(i);
                    // Do not take org.springframework.ai.document.Document#id as the value of
                    // io.army.spring.ai.vectorstore.SpringAiVectorStore#id. The former is uncertain,
                    // and such usage represents bad design.
                    o = this.constructor.get();
                    o.setDocumentId(document.getId())
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
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.id.in(SQLs::rowParam, idList))
                    .asDelete();
        } else {
            final List<Map<String, String>> paramList = new ArrayList<>(idCount);
            for (String id : idList) {
                paramList.add(Map.of(this.id.fieldName(), id));
            }
            stmt = SQLs.batchSingleDelete()
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.id.spaceEqual(SQLs::namedParam))
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
                .deleteFrom(this.tableMeta, AS, "t")
                .where(this.metadata.space(Postgres.AT_AT, SQLs.literal(JsonPathType.INSTANCE, jsonPath)))
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
                    s.selection(this.documentId, this.content, this.metadata);
                    if (operator == SQLs.NEG_DOT) {
                        s.selection(SQLs.LITERAL_1.plus(this.embedding.space(operator, embedding)).as(distanceLabel));
                    } else {
                        s.selection(this.embedding.space(operator, embedding).as(distanceLabel));
                    }
                })
                .from(this.tableMeta, AS, "t")
                .where(wb -> {
                    if (operator == SQLs.NEG_DOT) {
                        wb.accept(SQLs.LITERAL_1.plus(this.embedding.space(operator, embedding)).less(distance));
                    } else {
                        wb.accept(this.embedding.space(operator, embedding).less(distance));
                    }
                    if (jsonPath != null) {
                        wb.accept(this.metadata.space(Postgres.AT_AT, SQLs.literal(JsonPathType.INSTANCE, jsonPath)));
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
                    .id(row.getNonNull(0, String.class))
                    .text(row.get(1, String.class))
                    .metadata(metadata)
                    .score(1.0 - distanceValue)
                    .build();
        };

        final Function<SyncSession, List<Document>> function;
        function = session -> session.queryRecordList(stmt, rowFunc);

        final String sessionName = getClass().getName() + '.' + "doSimilaritySearch";
        return this.sessionContext.executeNotNull(sessionName, true, function);
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
                .collectionName(this.tableMeta.tableName())
                .dimensions(this.embedding.precision())
                .namespace(nameSpace)
                .similarityMetric(metric.value());
    }


    private EmbeddingOptions embeddingOptions() {
        final EmbeddingOptions.Builder builder = EmbeddingOptions.builder();
        if (this.model != null) {
            builder.model(this.model);
        }
        builder.dimensions(this.embedding.precision());
        return builder.build();
    }


    private Insert postgreInsertOrUpdateStmt(final List<T> rowList) {

        return Postgres.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta).as("t")
                .values(rowList)
                .onConflict().parens(s -> s.space(this.id))
                .doUpdate()
                .set(this.content, Postgres.excluded(this.content))
                .set(this.metadata, Postgres.excluded(this.metadata))
                .set(this.embedding, Postgres.excluded(this.embedding))
                .asInsert();
    }

    private Insert mysqlInsertOrUpdateStmt(final List<T> rowList) {
        final String rowAlias = "r";
        return MySQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(rowList)
                .as(rowAlias)
                .onDuplicateKey()
                .update(this.content, SQLs.field(rowAlias, this.content))
                .comma(this.metadata, SQLs.field(rowAlias, this.metadata))
                .comma(this.embedding, SQLs.field(rowAlias, this.embedding))
                .asInsert();
    }


    public static <T extends SpringAiVectorStore> Builder<T> builder(EmbeddingModel embeddingModel,
                                                                     SyncSessionContext sessionContext,
                                                                     SimpleTableMeta<T> tableMeta) {
        return new Builder<>(embeddingModel, sessionContext, tableMeta);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SpringAiVectorStore> Supplier<T> constructorFunc(SimpleTableMeta<T> tableMeta) {
        Supplier<T> constructor;
        if (tableMeta.javaType() == SpringAiVectorStore.class) {
            final Supplier<SpringAiVectorStore> func = SpringAiVectorStore::new;
            constructor = (Supplier<T>) func;
        } else {
            constructor = ObjectAccessorFactory.pojoConstructor(tableMeta.javaType());
        }
        return constructor;
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

    public static final class Builder<T extends SpringAiVectorStore> extends AbstractVectorStoreBuilder<Builder<T>> {

        private final SyncSessionContext sessionContext;

        private final SimpleTableMeta<T> tableMeta;

        private NullMode nullMode = NullMode.DEFAULT;

        private LiteralMode literalMode = LiteralMode.DEFAULT;

        private int maxDocumentBatchSize = 1000;

        private int batchDeleteThreshold = 20;

        private String model;

        private DistanceType distanceType;

        private Builder(EmbeddingModel embeddingModel, SyncSessionContext sessionContext, SimpleTableMeta<T> tableMeta) {
            super(embeddingModel);
            this.sessionContext = sessionContext;
            this.tableMeta = tableMeta;
        }


        public Builder<T> maxDocumentBatchSize(int maxDocumentBatchSize) {
            this.maxDocumentBatchSize = maxDocumentBatchSize;
            return this;
        }

        public Builder<T> batchDeleteThreshold(int batchDeleteThreshold) {
            this.batchDeleteThreshold = batchDeleteThreshold;
            return this;
        }

        public Builder<T> nullMode(NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        public Builder<T> literalMode(LiteralMode literalMode) {
            this.literalMode = literalMode;
            return this;
        }

        public Builder<T> distanceType(DistanceType distanceType) {
            this.distanceType = distanceType;
            return this;
        }

        public Builder<T> mode(String mode) {
            this.model = mode;
            return this;
        }


        @Override
        public VectorStore build() {

            if (this.distanceType == null) {
                this.distanceType = findDistanceType();
            }

            return new ArmyVectorStore<>(this);
        }

        private DistanceType findDistanceType() {
            final FieldMeta<T> embedding = this.tableMeta.field("embedding");

            List<FieldMeta<T>> fieldList;
            String opclass;

            DistanceType distanceType = null;
            topLoop:
            for (IndexMeta<T> index : this.tableMeta.indexList()) {
                fieldList = index.fieldList();
                if (fieldList.size() != 1) {
                    continue;
                }
                if (!embedding.equals(fieldList.getFirst())) {
                    continue;
                }
                opclass = index.columnList().getFirst().opclass();
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
            } // top loop

            if (distanceType == null) {
                String m = String.format("%s no opclass and don't config %s", embedding,
                        DistanceType.class.getName());
                throw new IllegalArgumentException(m);
            }

            return distanceType;
        }


    } //

}
