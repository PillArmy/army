# army-agentscope-agentstate

Persist AgentScope agent state in a relational database. This module is an Army implementation of
`io.agentscope.core.state.AgentStateStore`: it exposes the standard AgentScope interface to callers and uses
Army's Criteria API underneath.

- Maven coordinates: `io.qinarmy:army-agentscope-agentstate`
- Required dependencies: `army-sync`, `io.agentscope:agentscope-core`
- Optional dialect dependencies: `army-postgre` (PostgreSQL), `army-mysql` (MySQL)
- Only **PostgreSQL** and **MySQL** are supported; any other dialect throws `IllegalStateException` at runtime
- Optimistic concurrency is supported: `supportsVersioning()` returns `true`

Source entry points:

- [ArmyAgentStateStore.java](src/main/java/io/army/agentscope/state/ArmyAgentStateStore.java)
- [AgentScopeSessions.java](src/main/java/io/army/agentscope/state/AgentScopeSessions.java)

---

## 1. What it stores

One row is one state item, addressed by four columns:

| Column | Meaning |
|---|---|
| `userId` | User ID; when `null`, the anonymous user ID is used |
| `sessionId` | Session ID |
| `stateKey` | State key, e.g. `agent_state`, `messages` |
| `itemIndex` | List index; fixed to `0` for single-value state |

Other columns:

| Column | Type | Description |
|---|---|---|
| `id` | `String` | Primary key |
| `createTime` | `LocalDateTime` | Army reserved field, written on insert |
| `updateTime` | `LocalDateTime` | Army reserved field, maintained on update |
| `version` | `Long` | Optimistic-lock version, starts at `0` and is incremented by the framework on each update |
| `stateValue` | `String` | JSON text of the state object, mapped to `io.army.mapping.PreferredJsonbType` |

Two storage shapes:

- **Single value**: `save(userId, sessionId, key, state)` writes one row with `itemIndex = 0`.
- **List**: `save(userId, sessionId, key, List<State>)` writes one row per element with `itemIndex = 0..n-1`,
  plus an extra row under `stateKey + ":_hash"` holding a hash of the list, which is used to decide whether the
  whole list needs to be rewritten.

> Note: when an empty list is passed to the list `save`, the method returns immediately and **does not** clear
> existing rows for that key.

`createTime`, `updateTime`, and `version` are managed by Army. Explicitly setting them in an `UPDATE` statement is
rejected by the framework.

---

## 2. Add the dependencies

```xml
<dependencies>
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-agentscope-agentstate</artifactId>
        <version>0.6.8-SNAPSHOT</version>
    </dependency>

    <!-- Pick exactly one dialect -->
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-postgre</artifactId>
        <version>0.6.8-SNAPSHOT</version>
    </dependency>
    <!-- or -->
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-mysql</artifactId>
        <version>0.6.8-SNAPSHOT</version>
    </dependency>

    <!-- JDBC driver for your database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.11</version>
    </dependency>
</dependencies>
```

`army-postgre` and `army-mysql` are declared `optional` in the module POM, so you must add one of them explicitly.
If you wire the session factory through Spring, also add `army-spring`.

---

## 3. Step 1: define the entity

Create a class extending `AgentScopeSessions` and add `@Table`:

```java
package my.pkg;

import io.army.annotation.Index;
import io.army.annotation.Table;
import io.army.agentscope.state.AgentScopeSessions;

@Table(
        name = "my_agent_session",
        indexes = @Index(
                name = "${DEFAULT_VALUE}",
                unique = true,
                fieldList = {"userId", "sessionId", "stateKey", "itemIndex"}
        ),
        comment = "Agent state table"
)
public class MyAgentSession extends AgentScopeSessions {
}
```

Three hard requirements:

1. **A no-arg constructor is required.** `ArmyAgentStateStore` instantiates entities through
   `ObjectAccessorFactory.pojoConstructor(...)`. The empty class above satisfies this by default.
2. **Exactly one unique index over `userId, sessionId, stateKey, itemIndex` is required.** `Builder.build()`
   validates all indexes; otherwise it throws:
   `AgentStateStore index must have 4 fields: userId, sessionId, stateKey, itemIndex and be unique`
3. **Do not declare several unique indexes that all match these four fields.** The validator keeps the last match;
   one such index is enough.

The Army annotation processor generates a static metamodel class `MyAgentSession_` containing:

```java
public static final SimpleTableMeta<MyAgentSession> T;
```

`MyAgentSession_.T` is the `SimpleTableMeta` instance the builder needs.

---

## 4. Step 2: write TableMeta.properties

File location:

```
src/main/resources/META-INF/army/TableMeta.properties
```

### 4.1 When the file is consulted

Army looks up this file only when an annotation value is written as a placeholder:

| Annotation value | Behavior |
|---|---|
| `${DEFAULT}` | Use the configured value; otherwise fall back to the built-in default |
| `${RUNTIME}` | A configured value is mandatory; otherwise `MetaException` |
| `${OPTIONAL}` | Skip the element when no value is configured (supported by some attributes only) |
| Literal text | Used as-is; a property with the same key is **not** applied |

Key format:

```
{fully.qualified.EntityClass}.{fieldName}.{AnnotationName}.{attributeName}
```

Indexes use an array index instead:

```
{fully.qualified.EntityClass}.IndexMeta[0].name
```

### 4.2 Keys relevant to this module

| Key | Purpose | Default |
|---|---|---|
| `{FQCN}.Table.name` | Table name | `${DEFAULT}` converts the class name to snake_case |
| `{FQCN}.Table.comment` | Table comment | `${DEFAULT}` derives a readable comment from the class name |
| `{FQCN}.Table.catalog` | Catalog | Empty for `${DEFAULT}` |
| `{FQCN}.Table.schema` | Schema | Empty for `${DEFAULT}` |
| `{FQCN}.Table.ddlMode` | Whether to generate `CREATE TABLE` | `true` when absent |
| `{FQCN}.{field}.Column.name` | Column name | snake_case |
| `{FQCN}.{field}.Column.comment` | Column comment | Generated |
| `{FQCN}.{field}.Column.precision` | Length / precision | String 255, Decimal 16, UUID 36, Vector 1024 |
| `{FQCN}.{field}.Column.scale` | Scale / fractional seconds | Decimal 3, temporal types 6 |
| `{FQCN}.{field}.Column.defaultValue` | Default value | Type-appropriate zero value |
| `{FQCN}.{field}.Column.collation` | Collation | Empty |
| `{FQCN}.{field}.Mapping.value` | Fully-qualified mapping type class | Inferred from the Java type |
| `{FQCN}.{field}.GeneratorStrategy` | Strategy in the form `ClassName[:JSON params]` | `id` falls back to `POST` (database auto-increment) |
| `{FQCN}.IndexMeta[i].name` | Index name | `${DEFAULT_VALUE}` derives it by convention |
| `{FQCN}.IndexMeta[i].type` | Index type, e.g. `hnsw`, `gin` | Empty |
| `{FQCN}.IndexMeta[i].field.{fieldName}.collation` | Index column collation | Empty |
| `{FQCN}.IndexMeta[i].field.{fieldName}.opclass` | Index column opclass | Empty |

### 4.3 Minimal configuration for this module

`AgentScopeSessions.id` is a `String`. If no generator strategy is configured, Army treats it as `POST`
(database-generated). Relational databases normally do not auto-generate varchar primary keys, so in practice you
should configure Snowflake8 explicitly:

```properties
# startTime is a millisecond timestamp; use a different value per table in the same database
my.pkg.MyAgentSession.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779290105917}
```

`Snowflake8GeneratorStrategy` supports `String`, `Long`, `long`, and `BigInteger` field types, so no `Mapping.value`
is needed here.

`userId`, `sessionId`, and `stateKey` are inherited with `${DEFAULT}` names and `${DEFAULT}` mappings. The default
string precision is **255**. If session IDs can be longer, raise it explicitly:

```properties
my.pkg.MyAgentSession.sessionId.Column.precision=512
```

### 4.4 A reference example from another module

The stock example's
[TableMeta.properties](../army-test-example/army-example-stock-data/src/main/resources/META-INF/army/TableMeta.properties)
looks like this:

```properties
io.army.example.stock.domain.StockChatMemory.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779103273327}
io.army.example.stock.domain.StockChatMemory.id.Mapping.value=io.army.mapping.SqlBigIntType
io.army.example.stock.domain.StockChatVectorStore.embedding.Column.precision=1024
io.army.example.stock.domain.StockChatVectorStore.conversationId.Mapping.value=io.army.mapping.SqlBigIntType
```

You can follow the same format, but note two differences:

- The stock example uses a numeric primary key, hence `SqlBigIntType`. This module's base class uses a `String`
  `id`, so do **not** copy that line.
- `embedding.Column.precision=1024` is for a vector column; this module has no vector field.

---

## 5. Step 3: create the store

### 5.1 Builder options

| Method | Required | Description |
|---|---|---|
| `sessionContext(SyncSessionContext)` | Yes | Army session context |
| `anonymousUserIdSupplier(Supplier<String>)` | Yes | Anonymous user ID used when `userId == null` |
| `nullMode(NullMode)` | No | Defaults to `NullMode.DEFAULT` |
| `literalMode(LiteralMode)` | No | Defaults to `LiteralMode.DEFAULT` |

Missing required options make `build()` throw:

```
sessionContext is required
tableMeta is required
anonymousUserIdSupplier is required
```

### 5.2 Plain Java

```java
SyncSessionContext sessionContext = ...; // provided by the Army session factory

AgentStateStore store = ArmyAgentStateStore.builder(MyAgentSession_.T)
        .sessionContext(sessionContext)
        .anonymousUserIdSupplier(() -> "anonymous")
        .build();
```

The generic parameter `D` is inferred as `MyAgentSession`; the builder accepts
`SimpleTableMeta<? extends AgentScopeSessions>`.

### 5.3 Spring Boot wiring

Set up the session factory and transaction manager as usual. This mirrors the
[stock example's DataSourceConfiguration](../army-test-example/army-example-stock-data/src/main/java/io/army/example/stock/config/DataSourceConfiguration.java):

```java
@Configuration
public class AgentStateConfiguration {

    @Bean
    public FieldGeneratorFactory fieldGeneratorFactory() {
        return StandaloneFieldGeneratorFactory.getInstance();
    }

    @Bean
    public ArmySyncSessionFactoryBean armySyncSessionFactory(DataSource dataSource,
                                                             FieldGeneratorFactory fieldGeneratorFactory) {
        ArmySyncSessionFactoryBean factoryBean = ArmySyncSessionFactoryBean.create();
        factoryBean
                .setFactoryName("army-agent-state")
                .setDataSource(dataSource)
                .setPackagesToScan(List.of("my.pkg"))   // replace with your entity package
                .setFieldGeneratorFactory(fieldGeneratorFactory);
        return factoryBean;
    }

    @Bean
    public ArmySyncLocalTransactionManager armySyncTransactionManager(SyncSessionFactory sessionFactory) {
        ArmySyncLocalTransactionManager manager = ArmySyncLocalTransactionManager.create(sessionFactory);
        manager.setNestedTransactionAllowed(true);
        return manager;
    }

    @Bean
    public SyncSessionContext syncSessionContext(ArmySyncLocalTransactionManager manager) {
        return manager.getSessionContext();
    }

    @Bean
    public AgentStateStore agentStateStore(SyncSessionContext sessionContext) {
        return ArmyAgentStateStore.builder(MyAgentSession_.T)
                .sessionContext(sessionContext)
                .anonymousUserIdSupplier(() -> "anonymous")
                .build();
    }
}
```

> `setPackagesToScan` must include the package containing your entity, otherwise Army will not discover the
> `@Table` class.

---

## 6. Step 4: use it

First define a state type that can be serialized to JSON. AgentScope's `State` is just a marker interface:

```java
public record AgentStatusState(String status, int step) implements State {
}
```

### 6.1 Single value

```java
AgentStateStore store = ...;

store.save("alice", "session-1", "agent_state",
        new AgentStatusState("running", 3));

Optional<AgentStatusState> loaded =
        store.get("alice", "session-1", "agent_state", AgentStatusState.class);

loaded.ifPresent(s -> System.out.println(s.status() + " / " + s.step()));
```

`save` is a full replacement: when `(userId, sessionId, key)` already exists, the dialect-specific upsert is used
(`ON CONFLICT` for PostgreSQL, `ON DUPLICATE KEY UPDATE` for MySQL) and only `stateValue` is updated.

### 6.2 Optimistic locking

```java
VersionedState<AgentStatusState> current =
        store.getVersioned("alice", "session-1", "agent_state", AgentStatusState.class);

long newVersion = store.saveIfVersion(
        "alice", "session-1", "agent_state",
        new AgentStatusState("done", current.value().step()),
        current.version()
);

if (newVersion == AgentStateStore.UNVERSIONED) {
    // Version conflict: nothing was written
}
```

The three `expectedVersion` cases:

| Value | Behavior |
|---|---|
| `0` | Create if absent; if present with version `0`, upgrade to `1`; if present with a non-zero version, conflict |
| `AgentStateStore.UNVERSIONED` (`-1`) | Unconditional overwrite |
| Any other value | Write only when the stored version equals this value |

On success the new version is returned; on conflict `UNVERSIONED` is returned and nothing is written.
When the row does not exist, `getVersioned` returns `new VersionedState<>(null, 0L)`.

### 6.3 Lists

```java
List<AgentStatusState> list = List.of(
        new AgentStatusState("step-1", 1),
        new AgentStatusState("step-2", 2)
);

store.save("alice", "session-1", "history", list);

List<AgentStatusState> loadedList =
        store.getList("alice", "session-1", "history", AgentStatusState.class);
```

- Writes are stored in `itemIndex` order.
- Reads are returned in ascending `itemIndex` order.
- When the list is unchanged, the hash in `history:_hash` lets the store skip a rewrite; appended elements are
  written as new rows only.

### 6.4 Session-level operations

```java
boolean exists = store.exists("alice", "session-1");

Set<String> sessions = store.listSessionIds("alice"); // null lists the anonymous namespace
store.delete("alice", "session-1", "agent_state");    // delete one key
store.delete("alice", "session-1");                   // delete the whole session
```

With `listSessionIds(null)`, the anonymous ID from `anonymousUserIdSupplier` is used, so all anonymous sessions
share one namespace.

### 6.5 API summary

| Method | Description |
|---|---|
| `save(userId, sessionId, key, State)` | Full replacement of a single value |
| `save(userId, sessionId, key, List<? extends State>)` | List save (hash check + incremental append) |
| `get(userId, sessionId, key, Class<T>)` | Read a single value, `Optional.empty()` when absent |
| `getVersioned(...)` | Read a single value with its version |
| `getList(...)` | Read a list ordered by `itemIndex` ascending |
| `saveIfVersion(...)` | CAS write |
| `exists(userId, sessionId)` | Whether the session exists |
| `delete(userId, sessionId)` | Delete all rows of the session |
| `delete(userId, sessionId, key)` | Delete one key within the session |
| `listSessionIds(userId)` | List session IDs for a user |

---

## 7. Validation and limits

Possible exceptions:

| Message | Cause | Fix |
|---|---|---|
| `sessionContext is required` | No session context passed to the builder | Call `.sessionContext(...)` |
| `tableMeta is required` | The metamodel argument was `null` | Pass `MyAgentSession_.T` |
| `anonymousUserIdSupplier is required` | No anonymous ID supplier | Call `.anonymousUserIdSupplier(...)` |
| `AgentStateStore index must have 4 fields: userId, sessionId, stateKey, itemIndex and be unique` | `@Index` does not match the required four-field unique index | Fix `@Table` as described in section 3 |
| `AgentStateStore userId cannot be null or empty` | A blank ID was passed | Pass a non-blank ID |
| `AgentStateStore sessionId cannot exceed 255 characters` | The value exceeds the field precision | Configure `sessionId.Column.precision` |
| `IllegalStateException: ... is supported` | A dialect other than MySQL/PostgreSQL | Use a supported database |

Additional notes:

- ID length validation applies only to string mappings, detected via
  `field.mappingType() instanceof MappingType.SqlString` with `precision > 0`.
- `stateValue` is mapped with the literal `@Mapping("io.army.mapping.PreferredJsonbType")` on the base class.
  Because it is not a `${DEFAULT}` placeholder, `TableMeta.properties` cannot override it.
- If the table name is a literal (e.g. `name = "my_agent_session"`), the `Table.name` property has no effect.

---

## 8. Module relationships

```
agentscope-core  (AgentStateStore / State / VersionedState)
       +
army-sync  ──────> army-agentscope-agentstate
       +
army-postgre / army-mysql  (optional)
```

Related documentation:

- [army-spring-ai-model-chat-memory](../army-spring-ai-model-chat-memory/README.md): another module that persists agent data
- [army-example-stock-data](../army-test-example/army-example-stock-data/README.md): complete entity, DAO, and configuration example
- [Army session management skill](../.trae/skills/army-session-management/SKILL.md): session factory and transaction configuration
