# army-spring-ai-vector-store

This module is an implementation of `org.springframework.ai.vectorstore.VectorStore` for integrating Army ORM with
Spring AI's vector store abstraction.

## Features

- **PostgreSQL Vector Support**: Built-in pgvector integration for similarity search
- **MySQL Support**: Support for MySQL vector types
- **Chat Memory Integration**: Specialized support for AI agent long-term memory
- **Batch Operations**: Efficient batch insert/update with configurable batch size
- **Type-Safe**: Uses Army's compile-time metamodel for type-safe queries
- **Tool Integration**: Built-in `memoryTool()` for AI agent tool calling
- **Embedding Model Integration**: Works with any Spring AI EmbeddingModel

## Usage

### 1. Domain Class Definition

#### Generic Vector Store

```java

@Table(name = "document_vector_store")
public class DocumentVectorStore extends SpringAiVectorStore {
    // Automatically inherits: id, createTime, updateTime, version,
    // documentId, content, metadata, embedding
}
```

#### Chat Memory Vector Store (Long-Term Memory)

```java

@Table(name = "coder_chat_vector_store")
public class CoderChatVectorStore extends SpringAiChatVectorStore {
    // Automatically inherits: conversationId + all fields from SpringAiVectorStore
}
```

### 2. Configuration

```java

@Bean
public ArmyVectorStore<CoderChatVectorStore> chatVectorStore(
        EmbeddingModel embeddingModel,
        SyncSessionContext context) {
    return ArmyVectorStore.builder(embeddingModel, context, CoderChatVectorStore_.T)
            .maxDocumentBatchSize(1000)      // Default: 1000
            .batchDeleteThreshold(20)         // Default: 20
            .distanceType(ArmyVectorStore.DistanceType.COSINE_DISTANCE)
            .mode("text-embedding-3-small")   // Embedding model name
            .build();
}
```

### 3. Usage in ChatClient

```java

@Bean
public ChatClient coderChatClient(ChatClient.Builder builder,
                                  ArmyVectorStore<CoderChatVectorStore> vectorStore) {
    List<Advisor> advisorList = List.of(
            ArmyVectorStoreChatMemoryAdvisor.builder(vectorStore).build()
    );
    builder.defaultAdvisors(advisorList);
    builder.defaultTools(vectorStore.memoryTool(null));  // LongTermMemory tool
    return builder.build();
}
```

### 4. Memory Tool

The `memoryTool()` method provides a tool for AI agent to query long-term memory:

```java
vectorStore.memoryTool("LongTermMemory");
```

The tool accepts `MemoryCall` parameters:

| Parameter             | Type      | Description                                |
|-----------------------|-----------|--------------------------------------------|
| `conversationId`      | `String`  | Required. Conversation ID                  |
| `query`               | `String`  | Required. Query text for similarity search |
| `similarityThreshold` | `Double`  | Optional. Similarity threshold (0-1)       |
| `topK`                | `Integer` | Optional. Max results to return            |

## Key Classes

### ArmyVectorStore

**VectorStore implementation for Army ORM:**

**Core Methods:**

- `add(List<Document>)`: Inserts documents with embeddings (upsert semantics)
- `delete(List<String>)`: Deletes documents by document ID
- `delete(Filter.Expression)`: Deletes documents matching filter
- `similaritySearch(SearchRequest)`: Performs similarity search
- `memoryTool(String)`: Creates a tool for AI agent tool calling

**Builder Configuration:**

| Method                       | Type           | Default     | Description                             |
|------------------------------|----------------|-------------|-----------------------------------------|
| `maxDocumentBatchSize(int)`  | `int`          | 1000        | Batch size for insert operations        |
| `batchDeleteThreshold(int)`  | `int`          | 20          | Threshold for switching to batch delete |
| `nullMode(NullMode)`         | `NullMode`     | DEFAULT     | NULL handling mode                      |
| `literalMode(LiteralMode)`   | `LiteralMode`  | DEFAULT     | Literal handling mode                   |
| `distanceType(DistanceType)` | `DistanceType` | auto-detect | Distance metric                         |
| `mode(String)`               | `String`       | null        | Embedding model name                    |

**Distance Types:**

| Type              | Operator               | Description                |
|-------------------|------------------------|----------------------------|
| `L2_DISTANCE`     | `<->`                  | L2 distance (pgvector)     |
| `COSINE_DISTANCE` | `<=>`                  | Cosine distance (pgvector) |
| `NEG_DOT`         | negative inner product | Negative inner product     |

**Auto-detection:**
The distance type is automatically detected from the embedding column's index opclass:

- `vector_l2_ops` → `L2_DISTANCE`
- `vector_cosine_ops` → `COSINE_DISTANCE`
- `vector_ip_ops` → `NEG_DOT`

### SpringAiVectorStore

**Base domain class for vector store:**

| Field        | Type                  | Description                |
|--------------|-----------------------|----------------------------|
| `id`         | `String`              | Primary key (configurable) |
| `createTime` | `LocalDateTime`       | Creation timestamp         |
| `updateTime` | `LocalDateTime`       | Last update timestamp      |
| `version`    | `Integer`             | Optimistic lock            |
| `documentId` | `String`              | Document identifier        |
| `content`    | `String`              | Document content           |
| `metadata`   | `Map<String, Object>` | Document metadata (JSONB)  |
| `embedding`  | `float[]`             | Vector embedding           |

### SpringAiChatVectorStore

**Subclass for AI agent long-term memory:**

| Field            | Type     | Description             |
|------------------|----------|-------------------------|
| `conversationId` | `String` | Conversation identifier |

**Note:** When using `SpringAiChatVectorStore`, the `conversationId` field must be indexed.

### ArmyVectorStoreChatMemoryAdvisor

**ChatClient Advisor for automatic vector store management:**

```java
ArmyVectorStoreChatMemoryAdvisor.builder(vectorStore)
        .

order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER)
        .

scheduler(BaseAdvisor.DEFAULT_SCHEDULER)
        .

build();
```

**Behavior:**

- Saves user messages to vector store before the chat request
- Saves assistant messages to vector store after the chat response

**Important Design Note:**
This advisor **only performs save operations** and does NOT automatically retrieve and inject long-term memory into the
prompt. Long-term memory is sent to the AI model through the `memoryTool()` method (configured as `defaultTools`),
allowing the AI agent to actively query long-term memory when needed through similarity search. This is more efficient
than sending all memory with every request.

```java
// Long-term memory is provided as a tool, not injected into every prompt
builder.defaultTools(vectorStore.memoryTool(null));  // LongTermMemory tool
```

See `ArmyVectorStore#memoryTool` for details on the long-term memory retrieval tool.

### MemoryCall

**Record class for memory tool parameters:**

```java
public record MemoryCall(
        @ToolParam(description = "conversation id") String conversationId,
        @ToolParam(description = "query text") String query,
        @ToolParam(description = "similarity threshold", required = false) Double similarityThreshold,
        @ToolParam(description = "top k", required = false) Integer topK
) {
}
```

## PostgreSQL Setup

### pgvector Extension

```sql
-- Enable pgvector extension
CREATE
EXTENSION IF NOT EXISTS vector;

-- Create table for chat memory
CREATE TABLE coder_chat_vector_store
(
    id              BIGSERIAL PRIMARY KEY,
    create_time     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    version         INT                   DEFAULT 0,
    document_id     VARCHAR(36)  NOT NULL,
    content         TEXT,
    metadata        JSONB        NOT NULL DEFAULT '{}',
    embedding       vector(1536), -- Dimension must match your embedding model
    conversation_id VARCHAR(255) NOT NULL
);

-- Create index for conversation_id
CREATE INDEX idx_conversation_id ON coder_chat_vector_store (conversation_id);

-- Create vector index (choose one based on distance type)
-- For L2 distance
CREATE INDEX idx_embedding_l2 ON coder_chat_vector_store
    USING ivfflat (embedding vector_l2_ops);

-- For cosine distance
CREATE INDEX idx_embedding_cosine ON coder_chat_vector_store
    USING ivfflat (embedding vector_cosine_ops);

-- For inner product
CREATE INDEX idx_embedding_ip ON coder_chat_vector_store
    USING ivfflat (embedding vector_ip_ops);
```

## Configuration Summary

```java

@Configuration
public class CoderAiConfiguration {

    @Bean
    public ArmyVectorStore<CoderChatVectorStore> chatVectorStore(
            EmbeddingModel embeddingModel,
            SyncSessionContext context) {
        return ArmyVectorStore.builder(embeddingModel, context, CoderChatVectorStore_.T)
                .build();
    }

    @Bean
    public ChatClient coderChatClient(ChatClient.Builder builder,
                                      ArmyVectorStore<CoderChatVectorStore> vectorStore) {
        List<Advisor> advisorList = List.of(
                ArmyVectorStoreChatMemoryAdvisor.builder(vectorStore).build(),
                ToolCallingAdvisor.builder().build()
        );
        builder.defaultAdvisors(advisorList);
        builder.defaultTools(vectorStore.memoryTool(null));
        return builder.build();
    }
}
```

## Module Dependencies

```
army-core (Army framework core)
    ↓
army-spring-ai-vector-store
    ↓
spring-ai-vectorstore (Spring AI vector store module)
pgvector (PostgreSQL vector extension)
```

## Supported Databases

| Database   | Support | Notes                        |
|------------|---------|------------------------------|
| PostgreSQL | ✅ Full  | Requires pgvector extension  |
| MySQL      | ✅ Full  | Requires vector type support |