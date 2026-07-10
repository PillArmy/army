# army-spring-ai-model-chat-memory

This module is an implementation of `org.springframework.ai.chat.memory.ChatMemory` and
`org.springframework.ai.chat.memory.ChatMemoryRepository`.

## Features

- **Message Window Management**: Supports configurable max message count with automatic retention
- **Append-Only Storage**: Messages are appended without deleting previous ones (unlike Spring AI's
  `MessageWindowChatMemory`)
- **Full Replace Repository**: Lower-level persistence layer with delete-then-insert semantics
- **Tool Integration**: Built-in `memoryTool()` for AI agent tool calling
- **Type-Safe**: Uses Army's compile-time metamodel for type-safe queries

## Usage

### 1. Domain Class Definition

```java

@Table(name = "coder_chat_memory")
public class CoderChatMemory extends SpringAiChatMemory {
    // Automatically inherits: id, createTime, updateTime, version, 
    // conversationId, content, specializedData, type
}
```

### 2. ArmyMessageChatMemory (ChatMemory)

**Configuration:**

```java

@Bean
public ArmyMessageChatMemory<CoderChatMemory> coderChatMemory(SyncSessionContext context) {
    return ArmyMessageChatMemory.builder(context, CoderChatMemory_.T)
            .maxMessages(30)        // Max messages per conversation
            .build();
}
```

**Usage in ChatClient:**

```java

@Bean
public ChatClient coderChatClient(ChatClient.Builder builder,
                                  ArmyMessageChatMemory<CoderChatMemory> chatMemory) {
    List<Advisor> advisorList = List.of(
            ArmyMessageChatMemoryAdvisor.builder(chatMemory).build()
    );
    builder.defaultAdvisors(advisorList);
    builder.defaultTools(chatMemory.memoryTool(null));  // ShortTermMemory tool
    return builder.build();
}
```

### 3. ArmyChatMemoryRepository (ChatMemoryRepository)

**Configuration:**

```java

@Bean
public ArmyChatMemoryRepository<CoderChatMemory> chatMemoryRepository(SyncSessionContext context) {
    return ArmyChatMemoryRepository.builder(context, CoderChatMemory_.T)
            .build();
}
```

**Usage with Spring AI's MessageWindowChatMemory:**

```java
MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder(repository)
        .maxMessages(20)
        .build();
```

### 4. Memory Tool

Both classes provide a `memoryTool()` method for AI agent tool calling:

```java
// Short-term memory (from ArmyMessageChatMemory)
chatMemory.memoryTool("ShortTermMemory");

// Short-term memory (from ArmyChatMemoryRepository)
repository.

memoryTool("ShortTermMemory");
```

The tool accepts `MemoryCall` parameters:

| Parameter        | Type      | Description                  |
|------------------|-----------|------------------------------|
| `conversationId` | `String`  | Required. Conversation ID    |
| `maxRow`         | `Integer` | Optional. Max rows to return |

## Key Classes

### ArmyMessageChatMemory

**Higher-level ChatMemory implementation:**

- `add(String conversationId, List<Message>)`: Appends messages to storage
- `get(String conversationId)`: Retrieves up to `maxMessages` most recent messages (DESC order)
- `clear(String conversationId)`: Deletes all messages for a conversation

**Key Differences from Spring AI's MessageWindowChatMemory:**

- Does NOT delete previous messages in `add()`
- Messages are retained in storage, only limited during retrieval
- Automatic message retention via `maxMessages` during `get()`

### ArmyChatMemoryRepository

**Lower-level persistence layer:**

- `saveAll(String conversationId, List<Message>)`: FULL REPLACE (delete existing then insert)
- `findByConversationId(String conversationId)`: Returns ALL messages in chronological order (ASC)
- `findConversationIds()`: Returns distinct conversation IDs
- `deleteByConversationId(String conversationId)`: Deletes all messages for a conversation

**Key Differences from ArmyMessageChatMemory:**

- No message windowing logic (`maxMessages` is ChatMemory's responsibility)
- `saveAll()` replaces all messages, does NOT append
- `findByConversationId()` returns all messages without limit

### SpringAiChatMemory

**Base domain class for chat memory:**

| Field             | Type            | Description                          |
|-------------------|-----------------|--------------------------------------|
| `id`              | `Long`          | Primary key                          |
| `createTime`      | `LocalDateTime` | Creation timestamp                   |
| `conversationId`  | `String`        | Conversation identifier              |
| `content`         | `String`        | Message content                      |
| `specializedData` | `String`        | JSONB field for tool calls/responses |
| `type`            | `MessageType`   | User, System, Assistant, or Tool     |

### ArmyMessageChatMemoryAdvisor

**ChatClient Advisor for automatic memory management:**

```java
ArmyMessageChatMemoryAdvisor.builder(chatMemory)
        .

order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER)
        .

scheduler(BaseAdvisor.DEFAULT_SCHEDULER)
        .

build();
```

**Behavior:**

- Saves user messages before the chat request
- Saves assistant messages after the chat response

**Important Design Note:**
This advisor **only performs save operations** and does NOT automatically retrieve and inject memory into the prompt.
Memory is sent to the AI model through the `memoryTool()` method (configured as `defaultTools`), allowing the AI agent
to actively query short-term memory when needed. This is more efficient than sending all memory with every request.

```java
// Memory is provided as a tool, not injected into every prompt
builder.defaultTools(chatMemory.memoryTool(null));  // ShortTermMemory tool
```

See `ArmyChatMemorySupport#memoryTool` for details on the memory retrieval tool.

### SystemMessageAdvisor

**Adds conversation ID to system messages:**

```java
SystemMessageAdvisor.of(Ordered.HIGHEST_PRECEDENCE +10);
```

### MemoryCall

**Record class for memory tool parameters:**

```java
public record MemoryCall(
        @ToolParam(description = "conversation id") String conversationId,
        @ToolParam(description = "max row count", required = false) Integer maxRow
) {
}
```

## Configuration Summary

```java

@Configuration
public class CoderAiConfiguration {

    @Bean
    public ArmyMessageChatMemory<CoderChatMemory> coderChatMemory(SyncSessionContext context) {
        return ArmyMessageChatMemory.builder(context, CoderChatMemory_.T)
                .maxMessages(30)
                .build();
    }

    @Bean
    public ChatClient coderChatClient(ChatClient.Builder builder,
                                      ArmyMessageChatMemory<CoderChatMemory> chatMemory) {
        List<Advisor> advisorList = List.of(
                SystemMessageAdvisor.of(Ordered.HIGHEST_PRECEDENCE + 10),
                ArmyMessageChatMemoryAdvisor.builder(chatMemory).build(),
                ToolCallingAdvisor.builder().build()
        );
        builder.defaultAdvisors(advisorList);
        builder.defaultTools(chatMemory.memoryTool(null));
        return builder.build();
    }
}
```

## Module Dependencies

```
army-core (Army framework core)
    ↓
army-spring-ai-model-chat-memory
    ↓
spring-ai-chat (Spring AI chat module)
```