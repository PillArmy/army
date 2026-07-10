---
name: army-spring-ai-chat-memory
description: Army 框架 Spring AI Chat Memory 集成完整指南。涵盖 ArmyMessageChatMemory、ArmyChatMemoryRepository、ToolCallback 创建、长短期记忆（LongTermMemory/ShortTermMemory）实现。Invoke when user needs to integrate Army with Spring AI for chat memory, build memory tools, or configure conversation persistence.
---

# Army Spring AI Chat Memory

## 概述

`army-spring-ai-model-chat-memory` 模块将 Army 框架的数据库会话能力与 Spring AI 的 Chat Memory 集成，提供基于数据库的对话记忆持久化。

**模块路径**：`army-spring-ai-model-chat-memory/src/main/java/io/army/spring/ai/chat/memory/`

### 核心类架构

```
ArmyChatMemorySupport<T> (abstract)
    ├── ArmyMessageChatMemory<T>    → implements ChatMemory
    └── ArmyChatMemoryRepository<T> → implements ChatMemoryRepository

ArmyMessageChatMemoryAdvisor → implements ChatMemoryAdvisor
SystemMessageAdvisor          → implements CallAroundAdvisor
```

---

## 一、ArmyChatMemorySupport — 核心基类

### 1.1 职责

`ArmyChatMemorySupport<T extends SpringAiChatMemory>` 是所有记忆实现的核心基类，提供：

- SessionContext 管理
- TableMeta 字段映射
- 消息 CRUD 操作
- ToolCallback 创建

### 1.2 核心字段

| 字段                | 类型                    | 说明                                 |
|-------------------|-----------------------|------------------------------------|
| `sessionContext`  | `SyncSessionContext`  | 共享会话上下文                            |
| `tableMeta`       | `SimpleTableMeta<T>`  | 消息存储表元数据                           |
| `id`              | `PrimaryFieldMeta<T>` | 主键字段                               |
| `type`            | `FieldMeta<T>`        | 消息类型字段（USER/SYSTEM/ASSISTANT/TOOL） |
| `content`         | `FieldMeta<T>`        | 消息内容字段                             |
| `specializedData` | `FieldMeta<T>`        | 特殊数据字段（toolCalls/responses JSON）   |
| `conversationId`  | `FieldMeta<T>`        | 对话 ID 字段                           |
| `nullMode`        | `NullMode`            | NULL 处理模式                          |
| `literalMode`     | `LiteralMode`         | 字面量模式                              |

### 1.3 构建器模式

```java
// 构建器基类
static abstract class BuilderSupport<B extends BuilderSupport<B, T>, T extends SpringAiChatMemory> {
    B sessionContext(SyncSessionContext sessionContext);  // 必填
    B tableMeta(SimpleTableMeta<T> tableMeta);            // 必填
    B nullMode(NullMode nullMode);                        // 默认 DEFAULT
    B literalMode(LiteralMode literalMode);               // 默认 DEFAULT
    abstract ArmyChatMemorySupport<T> build();
}
```

### 1.4 消息序列化

消息通过 Army 的 `JsonCodec` 序列化：

```java
// 保存消息时的序列化
if (message instanceof AssistantMessage a) {
    o.setSpecializedData(codec.encode(a.getToolCalls()));
} else if (message instanceof ToolResponseMessage t) {
    o.setSpecializedData(codec.encode(t.getResponses()));
}

// 读取消息时的反序列化
codec.decodeList(specializedData, AssistantMessage.ToolCall.class);
codec.decodeList(specializedData, ToolResponseMessage.ToolResponse.class);
```

### 1.5 ToolCallback — 记忆工具

`memoryTool(String name)` 方法将记忆查询暴露为 Spring AI ToolCallback：

```java
public ToolCallback memoryTool(@Nullable String name) {
    if (name == null) {
        name = "ShortTermMemory";
    }
    return FunctionToolCallback
            .builder(name, this::getMemoryList)
            .description("Get short term memory")
            .inputType(MemoryCall.class)
            .build();
}
```

**MemoryCall 输入参数**：

```java
// MemoryCall 是 ToolCallback 的输入 DTO
public record MemoryCall(
    String conversationId,     // 必填：对话 ID
    @Nullable Integer maxRow   // 可选：最大返回行数
) {}
```

**生成的 SQL**（`getMemoryList` 内部）：

```sql
SELECT content, type, create_time
FROM chat_memory AS t
WHERE conversation_id = ?
ORDER BY id DESC
LIMIT ?
```

---

## 二、ArmyMessageChatMemory — ChatMemory 实现

### 2.1 特点

实现 `ChatMemory` 接口，**不删除旧消息**（与 `MessageWindowChatMemory` 不同）。

```java
public final class ArmyMessageChatMemory<T extends SpringAiChatMemory>
        extends ArmyChatMemorySupport<T> implements ChatMemory {
```

### 2.2 使用方式

```java
// 构建器创建
final SyncSessionContext sessionContext; // 从 Spring 注入
final SimpleTableMeta<SpringAiChatMemory> tableMeta; // 消息表元数据

final ArmyMessageChatMemory<SpringAiChatMemory> chatMemory;
chatMemory = ArmyMessageChatMemory
        .builder(sessionContext, tableMeta)
        .maxMessages(20)          // 每次查询最多返回的消息数
        .nullMode(NullMode.DEFAULT)
        .literalMode(LiteralMode.DEFAULT)
        .build();

// 作为 ToolCallback 暴露给 AI
final ToolCallback memoryTool = chatMemory.memoryTool("ShortTermMemory");
```

### 2.3 核心方法

| 方法                              | 说明                                |
|---------------------------------|-----------------------------------|
| `add(conversationId, messages)` | 追加消息（不删除旧消息）                      |
| `get(conversationId)`           | 获取最近 `maxMessages` 条消息（按 id DESC） |
| `clear(conversationId)`         | 删除该对话的所有消息                        |

### 2.4 get() 方法生成的 SQL

```sql
SELECT content, type, specialized_data, id
FROM chat_memory AS t
WHERE conversation_id = ?
ORDER BY id DESC
LIMIT ?
```

---

## 三、ArmyChatMemoryRepository — 持久层实现

### 3.1 特点

实现 `ChatMemoryRepository` 接口，提供更低级别的持久化操作。

**与 ChatMemory 的区别**：

| 功能       | `ArmyMessageChatMemory` | `ArmyChatMemoryRepository` |
|----------|-------------------------|----------------------------|
| 保存策略     | 追加（append）              | 全量替换（先删后插）                 |
| 查询限制     | 有 `maxMessages` 限制      | 无限制，全量返回                   |
| 对话 ID 查询 | ×                       | `findConversationIds()`    |
| 窗口逻辑     | 自带                      | 无（交给上层）                    |

```java
public final class ArmyChatMemoryRepository<T extends SpringAiChatMemory>
        extends ArmyChatMemorySupport<T> implements ChatMemoryRepository {
```

### 3.2 核心方法

| 方法                                       | 说明                                        |
|------------------------------------------|-------------------------------------------|
| `saveAll(conversationId, messages)`      | 先删除旧消息，再插入新消息（全量替换）                       |
| `findByConversationId(conversationId)`   | 返回该对话的全部消息（按 id ASC）                      |
| `findConversationIds()`                  | 返回所有不同的 conversationId（`SELECT DISTINCT`） |
| `deleteByConversationId(conversationId)` | 删除对话消息                                    |

### 3.3 findConversationIds() SQL

```sql
SELECT DISTINCT conversation_id
FROM chat_memory AS t
```

---

## 四、ArmyMessageChatMemoryAdvisor — 对话记忆顾问

### 4.1 职责

实现 Spring AI 的 `ChatMemoryAdvisor`，在每次 AI 对话中自动注入历史消息。

```java
// 使用方式
final ChatClient chatClient;
chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(
            new ArmyMessageChatMemoryAdvisor(chatMemory)
        )
        .build();
```

Advisor 在每次调用时：

1. 从 `ChatMemory` 读取历史消息
2. 将历史消息注入到当前请求的 messages 列表中
3. 响应后将新消息存入 `ChatMemory`

---

## 五、SystemMessageAdvisor

### 5.1 职责

注入系统级提示词，通过 `CallAroundAdvisor` 实现。

```java
// 注入静态系统消息
final SystemMessageAdvisor advisor;
advisor = new SystemMessageAdvisor("You are a helpful assistant.");
```

---

## 六、Domain 类要求

### 6.1 SpringAiChatMemory 接口

消息表对应的 domain 类必须实现 `SpringAiChatMemory` 接口：

```java
public interface SpringAiChatMemory {
    String getConversationId();
    SpringAiChatMemory setConversationId(String conversationId);
    
    String getContent();
    SpringAiChatMemory setContent(String content);
    
    MessageType getType();
    SpringAiChatMemory setType(MessageType type);
    
    String getSpecializedData();
    SpringAiChatMemory setSpecializedData(String specializedData);
}
```

### 6.2 数据库表结构要求

| 列名                 | 类型                   | 说明                                  |
|--------------------|----------------------|-------------------------------------|
| `id`               | BIGINT (PRIMARY KEY) | 主键，自增或 Snowflake                    |
| `conversation_id`  | VARCHAR              | 对话 ID（会话级别唯一标识）                     |
| `type`             | VARCHAR/ENUM         | 消息类型：USER/SYSTEM/ASSISTANT/TOOL     |
| `content`          | TEXT                 | 消息文本内容                              |
| `specialized_data` | TEXT                 | JSON 格式的特殊数据（ToolCall/ToolResponse） |
| `create_time`      | TIMESTAMP            | 创建时间                                |

---

## 七、完整集成示例

```java
@Configuration
public class ChatMemoryConfig {

    @Bean
    public ArmyMessageChatMemory<ChatMemoryDomain> chatMemory(
            SyncSessionContext sessionContext,
            SimpleTableMeta<ChatMemoryDomain> tableMeta) {
        
        return ArmyMessageChatMemory
                .builder(sessionContext, tableMeta)
                .maxMessages(50)
                .build();
    }

    @Bean
    public ChatMemoryAdvisor chatMemoryAdvisor(
            ArmyMessageChatMemory<ChatMemoryDomain> chatMemory) {
        return new ArmyMessageChatMemoryAdvisor(chatMemory);
    }

    @Bean
    public ToolCallback[] memoryTools(
            ArmyMessageChatMemory<ChatMemoryDomain> chatMemory) {
        return new ToolCallback[] {
            chatMemory.memoryTool("ShortTermMemory")
        };
    }
}
```

---

## 八、注意事项

1. **maxMessages**：默认 20，可通过 Builder 调整
2. **conversationId**：不能为 null 或空字符串
3. **ToolCallback**：`inputType(MemoryCall.class)` 要求 AI 模型支持结构化输入
4. **事务**：记忆操作通过 `SyncSessionContext.executeVoid/executeNotNull` 自动管理 session 生命周期
5. **保存策略差异**：`ArmyMessageChatMemory` 追加保存（不删除），`ArmyChatMemoryRepository` 全量替换

---

## Skill 进化机制

以下情况必须更新本 Skill：

1. 新增记忆类型（如 LongTermMemory）实现
2. MemoryCall 参数变更
3. 表结构字段变更
4. 新增 Advisor 类型
5. 发现新的使用模式或最佳实践
