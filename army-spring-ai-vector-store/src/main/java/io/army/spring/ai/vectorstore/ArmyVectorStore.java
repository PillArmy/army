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
import io.army.mapping.TextType;
import io.army.mapping.optional.JsonPathType;
import io.army.meta.*;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.util.RowMaps;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.army.criteria.impl.SQLs.AS;

/// Army implementation of Spring AI's {@link org.springframework.ai.vectorstore.VectorStore} interface.
///
/// This class provides a type-safe vector store implementation for Army ORM with the following features:
///
/// - **PostgreSQL Vector Support**: Built-in pgvector integration for similarity search
/// - **MySQL Support**: Support for MySQL vector types
/// - **Chat Memory Integration**: Specialized support for AI agent long-term memory
/// - **Batch Operations**: Efficient batch insert/update with configurable batch size
/// - **Tool Integration**: Built-in memory tool for AI agent calling
/// - **Type-Safe**: Uses Army's compile-time metamodel
///
/// ### Supported Distance Types:
/// - {@link DistanceType#L2_DISTANCE} - L2 distance (&lt;-&gt;)
/// - {@link DistanceType#COSINE_DISTANCE} - Cosine distance (&lt;=&gt;)
/// - {@link DistanceType#NEG_DOT} - Negative inner product
///
/// ### Usage:
/// ```java
/// ArmyVectorStore&lt;CoderChatVectorStore&gt; vectorStore = ArmyVectorStore.builder(embeddingModel, context, CoderChatVectorStore_.T)
///         .maxDocumentBatchSize(1000)
///         .distanceType(ArmyVectorStore.DistanceType.COSINE_DISTANCE)
///         .mode("text-embedding-3-small")
///         .build();
/// ```
///
/// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
/// @param <T> The domain class type extending {@link SpringAiVectorStore}
public final class ArmyVectorStore<T extends SpringAiVectorStore> extends AbstractObservationVectorStore {


    /// The null handling mode.
    private final NullMode nullMode;

    /// The literal handling mode.
    private final LiteralMode literalMode;

    /// Maximum batch size for insert operations.
    private final int maxDocumentBatchSize;

    /// Threshold for switching to batch delete mode.
    private final int batchDeleteThreshold;

    /// The Army session context.
    private final SyncSessionContext sessionContext;

    /// The distance type for similarity search.
    private final DistanceType distanceType;

    /// The SQL operator for distance calculation.
    private final SQLs.DualOperator operator;

    /// The embedding model name.
    private final String model;

    /// The compile-time metamodel for the domain class.
    private final SimpleTableMeta<T> tableMeta;

    /// Whether this store is used for chat memory.
    private final boolean chatStore;

    /// The primary key field.
    private final PrimaryFieldMeta<T> id;

    /// The document content field.
    private final FieldMeta<T> content;

    /// The document metadata field.
    private final FieldMeta<T> metadata;

    /// The vector embedding field.
    private final FieldMeta<T> embedding;

    /// The document ID field.
    private final FieldMeta<T> documentId;

    /// The conversation ID field (for chat memory stores).
    private final FieldMeta<T> conversationId;

    /// The field used for ON CONFLICT clause.
    private final FieldMeta<T> onConflictField;

    /// The domain class constructor.
    private final Supplier<T> constructor;

    /// The filter expression converter.
    private final FilterExpressionConverter converter;


    private ArmyVectorStore(Builder<T> builder) {
        super(builder);

        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;
        this.maxDocumentBatchSize = builder.maxDocumentBatchSize;
        this.batchDeleteThreshold = builder.batchDeleteThreshold;

        this.sessionContext = builder.sessionContext;
        this.distanceType = builder.distanceType;
        this.operator = this.distanceType.operator;
        this.model = builder.model;

        this.tableMeta = builder.tableMeta;

        this.chatStore = SpringAiChatVectorStore.class.isAssignableFrom(this.tableMeta.javaType());

        this.id = builder.tableMeta.id();

        this.content = this.tableMeta.field("content");
        this.metadata = this.tableMeta.field("metadata");
        this.embedding = this.tableMeta.field("embedding");
        this.documentId = this.tableMeta.field("documentId");

        if (this.chatStore) {
            this.conversationId = this.tableMeta.field("conversationId");
            if (!this.conversationId.isIndexField()) {
                throw new IllegalArgumentException(String.format("%s must be an index field", this.conversationId));
            }
        } else {
            this.conversationId = null;
        }

        if (this.documentId.isUniqueField()) {
            this.onConflictField = this.documentId;
        } else {
            this.onConflictField = this.id;
        }

        this.constructor = ObjectAccessorFactory.pojoConstructor(this.tableMeta.javaType());

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


    /// Adds documents to the vector store with batch processing.
    ///
    /// Embeds documents and inserts them into the database in batches.
    /// For chat memory stores, the metadata must contain a "conversationId" key.
    ///
    /// @param documents The documents to add
    @Override
    public void doAdd(final List<Document> documents) {

        final List<float[]> embeddingList;
        embeddingList = this.embeddingModel.embed(documents, embeddingOptions(), this.batchingStrategy);


        final Consumer<SyncSession> function;
        function = session -> {
            final Database database = session.dialectDatabase();

            final int documentCount = documents.size(), maxBatchSize = this.maxDocumentBatchSize;
            final List<T> rowList = new ArrayList<>(Math.min(documentCount, maxBatchSize));
            T o;
            Document document;
            Object conversationId;
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

                    if (this.chatStore) {
                        conversationId = document.getMetadata().get("conversationId");
                        if (conversationId == null) {
                            String m = String.format("metadata conversationId is required for %s", this.tableMeta);
                            throw new IllegalArgumentException(m);
                        }
                        ((SpringAiChatVectorStore) o).setConversationId(conversationId.toString());
                    }

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

        };

        final String sessionName = getClass().getName() + '.' + "add";
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    /// Deletes documents from the vector store.
    ///
    /// Uses single delete for small batches and batch delete for large batches.
    ///
    /// @param idList The document IDs to delete
    @Override
    public void doDelete(final List<String> idList) {
        final int idCount = idList.size();
        final DeleteStatement stmt;
        if (idCount < this.batchDeleteThreshold) {
            stmt = SQLs.singleDelete()
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.documentId.in(SQLs::rowParam, idList))
                    .asDelete();
        } else {
            final List<Map<String, String>> paramList = new ArrayList<>(idCount);
            for (String id : idList) {
                paramList.add(Map.of(this.id.fieldName(), id));
            }
            stmt = SQLs.batchSingleDelete()
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.documentId.spaceEqual(SQLs::namedParam))
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


    /// Deletes documents from the vector store using a filter expression.
    ///
    /// For chat memory stores, automatically filters by conversation ID if present in the filter.
    ///
    /// @param filterExpression The filter expression
    @Override
    protected void doDelete(Filter.Expression filterExpression) {
        final String jsonPath;
        jsonPath = this.converter.convertExpression(filterExpression);

        final IPredicate conversationIdPredicate;
        if (this.chatStore) {
            conversationIdPredicate = parseFirstLevelConversationIdPredicate(filterExpression);
        } else {
            conversationIdPredicate = null;
        }

        final Delete stmt;
        stmt = SQLs.singleDelete()
                .deleteFrom(this.tableMeta, AS, "t")
                .where(wb -> {
                    if (conversationIdPredicate != null) {
                        wb.accept(conversationIdPredicate);
                    }
                    wb.accept(this.metadata.space(Postgres.AT_AT, SQLs.literal(JsonPathType.INSTANCE, jsonPath)));
                })
                .asDelete();

        final Consumer<SyncSession> function;
        function = session -> session.update(stmt);

        final String sessionName = getClass().getName() + '.' + "deleteByExp";
        this.sessionContext.executeVoid(sessionName, false, function);
    }

    /// Performs similarity search on the vector store.
    ///
    /// Embeds the query text and searches for similar documents using the configured distance type.
    /// Results are sorted by distance (closest first).
    ///
    /// @param request The search request containing query, similarity threshold, and filter
    /// @return The list of matching documents with distance metadata
    @Override
    public List<Document> doSimilaritySearch(final SearchRequest request) {
        final Filter.Expression expression = request.getFilterExpression();

        final IPredicate conversationIdPredicate;
        final String jsonPath;
        if (expression == null) {
            jsonPath = null;
            conversationIdPredicate = null;
        } else {
            jsonPath = this.converter.convertExpression(expression);
            if (this.chatStore) {
                conversationIdPredicate = parseFirstLevelConversationIdPredicate(expression);
            } else {
                conversationIdPredicate = null;
            }
        }

        final double distance = 1 - request.getSimilarityThreshold();

        final float[] queryEmbedding;
        queryEmbedding = embeddingText(request.getQuery());

        final String distanceLabel = "distance";

        final Select stmt;
        stmt = SQLs.query()
                .select(this.documentId, this.content, this.metadata)
                .comma(distanceSelection(queryEmbedding, distanceLabel))
                .from(this.tableMeta, AS, "t")
                .where(wb -> {
                    if (conversationIdPredicate != null) {
                        wb.accept(conversationIdPredicate);
                    }
                    wb.accept(distancePredicate(queryEmbedding, distance));
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


    /// Creates a memory tool callback for AI agent calling.
    ///
    /// This tool allows AI agents to retrieve long-term memory through function calls.
    /// The tool accepts a {@link MemoryCall} parameter with conversationId and query fields.
    ///
    /// ### Usage in AI Agent:
    /// ```java
    /// ToolCallback memoryTool = vectorStore.memoryTool();
    /// ChatClient client = ChatClient.builder(chatModel)
    ///         .addTools(memoryTool)
    ///         .build();
    /// ```
    ///
    /// @param name The tool name (defaults to "LongTermMemory")
    /// @return The tool callback
    public ToolCallback memoryTool(@Nullable String name) {
        if (name == null) {
            name = "LongTermMemory";
        }
        return FunctionToolCallback
                .builder(name, this::getMemoryList)
                .description("Get long term memory")
                .inputType(MemoryCall.class)
                .build();
    }


    private Selection distanceSelection(float[] queryEmbedding, String label) {
        final Selection selection;
        if (this.operator == SQLs.NEG_DOT) {
            selection = SQLs.LITERAL_1.plus(this.embedding.space(this.operator, queryEmbedding)).as(label);
        } else {
            selection = this.embedding.space(this.operator, queryEmbedding).as(label);
        }
        return selection;
    }

    private IPredicate distancePredicate(float[] queryEmbedding, double distance) {
        final IPredicate predicate;
        if (this.operator == SQLs.NEG_DOT) {
            predicate = SQLs.LITERAL_1.plus(this.embedding.space(operator, queryEmbedding)).less(distance);
        } else {
            predicate = this.embedding.space(operator, queryEmbedding).less(distance);
        }
        return predicate;
    }

    private float[] embeddingText(String text) {
        return this.embeddingModel.call(new EmbeddingRequest(List.of(text), embeddingOptions()))
                .getResults()
                .stream()
                .map(Embedding::getOutput)
                .findFirst()
                .orElseThrow();
    }


    private List<Map<String, Object>> getMemoryList(MemoryCall call) {
        Objects.requireNonNull(call);

        final String conversationId, query;
        conversationId = call.conversationId();
        query = call.query();
        Objects.requireNonNull(conversationId);


        final Function<SyncSession, List<Map<String, Object>>> function;
        function = session -> {
            Double similarityThreshold = call.similarityThreshold();
            if (similarityThreshold == null) {
                similarityThreshold = SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL;
            }
            Integer topK = call.topK();
            if (topK == null) {
                topK = SearchRequest.DEFAULT_TOP_K;
            }

            final double distance = 1 - similarityThreshold;

            final float[] queryEmbedding;
            queryEmbedding = embeddingText(query);

            final String distanceLabel = "distance";

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.content, this.metadata.space(Postgres.DARROW, SQLs.literal(TextType.INSTANCE, "messageType")).as("messageType"))
                    .comma(distanceSelection(queryEmbedding, distanceLabel))
                    .comma(this.tableMeta.createTime())
                    .from(this.tableMeta, AS, "t")
                    .where(this.conversationId.equal(conversationId))
                    .and(this.distancePredicate(queryEmbedding, distance))
                    .orderBy(SQLs.refSelection(distanceLabel))
                    .limit(topK)
                    .asQuery();

            return session.queryObjectList(stmt, RowMaps.hashMapConstructor(4));
        };

        final String sessionName = getClass().getName() + '.' + "getMemoryList";
        return this.sessionContext.executeNotNull(sessionName, true, function);
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
                .onConflict().parens(s -> s.space(this.onConflictField))
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


    /// Parse the first-level conversationId value in the expression
    @Nullable
    private IPredicate parseFirstLevelConversationIdPredicate(final Filter.Expression expression) {

        final Filter.ExpressionType type = expression.type();
        return switch (type) {
            case OR, ISNULL, ISNOTNULL, NOT -> null;
            case AND -> parsePredicateInAndClause(expression.left(), expression.right());
            default -> parsePredicate(expression.left(), type, expression.right());
        };
    }

    @Nullable
    private IPredicate parsePredicateInAndClause(Filter.Operand left, Filter.Operand right) {
        if (!(left instanceof Filter.Expression leftExp && right instanceof Filter.Expression rightExp)) {
            return null;
        }
        Filter.ExpressionType type;
        IPredicate predicate;
        type = leftExp.type();
        if (type == Filter.ExpressionType.OR) {
            return null;
        }
        predicate = parsePredicate(leftExp.left(), type, leftExp.right());
        if (predicate != null) {
            return predicate;
        }
        type = rightExp.type();
        if (type == Filter.ExpressionType.OR) {
            return null;
        }
        return parsePredicate(rightExp.left(), rightExp.type(), rightExp.right());
    }


    @Nullable
    private IPredicate parsePredicate(Filter.Operand left, Filter.ExpressionType type, Filter.Operand right) {
        final IPredicate predicate;
        final String conversationId;
        switch (type) {
            case AND:
                predicate = parsePredicateInAndClause(left, right);
                break;
            case EQ: {
                conversationId = parseConversationIdFromExpression(left, right);
                if (StringUtils.hasText(conversationId)) {
                    predicate = this.conversationId.equal(conversationId);
                } else {
                    predicate = null;
                }
            }
            break;
            case GT: {
                conversationId = parseConversationIdFromExpression(left, right);
                if (StringUtils.hasText(conversationId)) {
                    predicate = this.conversationId.greater(conversationId);
                } else {
                    predicate = null;
                }
            }
            break;

            case LT: {
                conversationId = parseConversationIdFromExpression(left, right);
                if (StringUtils.hasText(conversationId)) {
                    predicate = this.conversationId.less(conversationId);
                } else {
                    predicate = null;
                }
            }
            break;
            case NE: {
                conversationId = parseConversationIdFromExpression(left, right);
                if (StringUtils.hasText(conversationId)) {
                    predicate = this.conversationId.notEqual(conversationId);
                } else {
                    predicate = null;
                }
            }
            break;
            case GTE: {
                conversationId = parseConversationIdFromExpression(left, right);
                if (StringUtils.hasText(conversationId)) {
                    predicate = this.conversationId.greaterEqual(conversationId);
                } else {
                    predicate = null;
                }
            }
            break;
            case LTE: {
                conversationId = parseConversationIdFromExpression(left, right);
                if (StringUtils.hasText(conversationId)) {
                    predicate = this.conversationId.lessEqual(conversationId);
                } else {
                    predicate = null;
                }
            }
            break;
            case IN: {
                final List<String> list;
                list = parseConversationIdListFromExpression(left, right);
                if (list.isEmpty()) {
                    predicate = null;
                } else {
                    predicate = this.conversationId.in(SQLs::rowParam, list);
                }
            }
            break;
            case NIN: {
                final List<String> list;
                list = parseConversationIdListFromExpression(left, right);
                if (list.isEmpty()) {
                    predicate = null;
                } else {
                    predicate = this.conversationId.notIn(SQLs::rowParam, list);
                }
            }
            break;
            default:
                predicate = null;
        }
        return predicate;
    }

    @Nullable
    private static String parseConversationIdFromExpression(Filter.Operand left, Filter.Operand right) {
        final String conversationId;
        if (!(left instanceof Filter.Key(String key) && right instanceof Filter.Value(Object value))) {
            conversationId = null;
        } else if (key.equals("conversationId") && value instanceof String) {
            conversationId = (String) value;
        } else {
            conversationId = null;
        }
        return conversationId;
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseConversationIdListFromExpression(Filter.Operand left, Filter.Operand right) {
        final List<String> conversationIdList;
        if (!(left instanceof Filter.Key(String key) && right instanceof Filter.Value(Object value))) {
            conversationIdList = List.of();
        } else if (key.equals("conversationId") && value instanceof List<?> list) {
            boolean match = false;
            for (Object o : list) {
                if (o == null || o instanceof String) {
                    match = true;
                } else {
                    match = false;
                    break;
                }
            }
            if (match) {
                conversationIdList = (List<String>) list;
            } else {
                conversationIdList = List.of();
            }
        } else {
            conversationIdList = List.of();
        }
        return conversationIdList;
    }


    public enum DistanceType {

        /// <-> - L2 distance
        ///
        /// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
        L2_DISTANCE(SQLs.L2_DISTANCE),

        ///  <=> - Cosine distance
        ///
        /// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
        COSINE_DISTANCE(SQLs.COSINE_DISTANCE),

        /// (negative) inner product
        NEG_DOT(SQLs.NEG_DOT);

        public final SQLs.DualOperator operator;

        DistanceType(SQLs.DualOperator operator) {
            this.operator = operator;
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
        public ArmyVectorStore<T> build() {
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
                switch (opclass) {
                    case "vector_l2_ops":
                        distanceType = DistanceType.L2_DISTANCE;
                        break topLoop;
                    case "vector_cosine_ops":
                        distanceType = DistanceType.COSINE_DISTANCE;
                        break topLoop;
                    case "vector_ip_ops":
                        distanceType = DistanceType.NEG_DOT;
                        break topLoop;
                } // switch

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
