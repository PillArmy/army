# army-agentscope

Persist AgentScope data in a relational database through Army's Criteria API. The module exposes the
standard AgentScope interfaces to callers and uses Army underneath — no JDBC, no hand-written SQL.

- Maven coordinates: `io.qinarmy:army-agentscope`
- Required dependencies: `army-sync`, `io.agentscope:agentscope-core`
- Optional dependencies: `army-postgre` (PostgreSQL), `army-mysql` (MySQL),
  `io.agentscope:agentscope-harness` (needed by the store / snapshot / sandbox features)
- Only **PostgreSQL** and **MySQL** are supported; any other dialect throws `IllegalStateException` at runtime

Four AgentScope interfaces are implemented:

| Package | Classes | Implements | AgentScope artifact |
|---|---|---|---|
| `state` | [AgentScopeSessions](src/main/java/io/army/agentscope/state/AgentScopeSessions.java), [ArmyAgentStateStore](src/main/java/io/army/agentscope/state/ArmyAgentStateStore.java) | `io.agentscope.core.state.AgentStateStore` | agentscope-core |
| `store` | [AgentScopeStore](src/main/java/io/army/agentscope/store/AgentScopeStore.java), [ArmyBaseStore](src/main/java/io/army/agentscope/store/ArmyBaseStore.java) | `io.agentscope.harness.agent.filesystem.remote.store.BaseStore` | agentscope-harness |
| `snapshot` | [AgentScopeSnapshot](src/main/java/io/army/agentscope/snapshot/AgentScopeSnapshot.java), [ArmyRemoteSnapshotClient](src/main/java/io/army/agentscope/snapshot/ArmyRemoteSnapshotClient.java) | `io.agentscope.harness.agent.sandbox.snapshot.RemoteSnapshotClient` | agentscope-harness |
| `sandbox` | [ArmySandboxExecutionGuard](src/main/java/io/army/agentscope/sandbox/ArmySandboxExecutionGuard.java) | `io.agentscope.harness.agent.sandbox.SandboxExecutionGuard` | agentscope-harness |

All four share the same pattern:

1. An **abstract `@MappedSuperclass` base entity** carries the columns; you subclass it and add `@Table`.
   (`ArmySandboxExecutionGuard` needs no entity at all — it uses native database locks.)
2. The implementation class is generic, `ArmyXxx<D extends BaseEntity>`, and receives the generated
   `SimpleTableMeta` (`MyEntity_.T`) through a builder.
3. The builder takes a `SyncSessionFactory`; every method opens its own `SyncSession` internally.

---

## 1. Add the dependencies

```xml
<dependencies>
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-agentscope</artifactId>
    </dependency>

    <!-- Pick exactly one dialect -->
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-postgre</artifactId>
    </dependency>
    <!-- or -->
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-mysql</artifactId>
    </dependency>

    <!-- Required for store / snapshot / sandbox features (optional in this module) -->
    <dependency>
        <groupId>io.agentscope</groupId>
        <artifactId>agentscope-harness</artifactId>
    </dependency>

    <!-- JDBC driver for your database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
</dependencies>
```

`army-postgre`, `army-mysql`, and `agentscope-harness` are declared `optional` in the module POM, so you must
add the ones you use explicitly. Versions are expected to come from your BOM or dependency management
(e.g. the Army parent POM manages all `io.qinarmy` artifacts). If you wire the session factory through
Spring, also add `army-spring`.

---

## 2. Common step: define your entity

Each base entity is abstract and carries Army reserved fields plus the feature columns. Subclass it in **your
own** project and add `@Table`. The Army annotation processor then generates a static metamodel class
`MyEntity_` containing `public static final SimpleTableMeta<MyEntity> T`.

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

Hard requirements, identical for every feature:

1. **A no-arg constructor is required.** Entities are instantiated through
   `ObjectAccessorFactory.pojoConstructor(...)`; the empty subclass above satisfies this by default.
2. **The unique index required by each feature must exist** (see the per-feature sections). `build()` validates
   it through `ValidateUtils.assertExistsIndex(...)` and throws `IllegalArgumentException` otherwise:
   `<table> not exists index[unique=true] thant have <n> fields: [...]`
3. **Do not declare several unique indexes over the same fields.** One is enough.

Reserved fields handled by Army (`id`, `createTime`, `updateTime`, `version`):

- `id` uses `@Generator(type = GeneratorType.DEFAULT)` — configure the concrete strategy in
  `TableMeta.properties` (section 7).
- `createTime` / `updateTime` are written by the framework.
- `version` is declared `defaultValue = "1"` and incremented by Army on each update; explicitly setting these
  fields in an `UPDATE` statement is rejected by the framework.

---

## 3. AgentStateStore (package `state`)

One row is one state item, addressed by four columns:

| Column | Meaning |
|---|---|
| `userId` | User ID; when `null`, the anonymous user ID is used |
| `sessionId` | Session ID |
| `stateKey` | State key, e.g. `agent_state`, `messages` |
| `itemIndex` | List index; fixed to `0` for single-value state |

Plus `id`, `createTime`, `updateTime`, `version` (Army reserved), and `stateValue` — the JSON text of the state
object, mapped to `io.army.mapping.PreferredJsonbType`.

Two storage shapes:

- **Single value**: `save(userId, sessionId, key, state)` writes one row with `itemIndex = 0`.
- **List**: `save(userId, sessionId, key, List<State>)` writes one row per element with `itemIndex = 0..n-1`,
  plus an extra row under `stateKey + ":_hash"` holding a hash of the list, used to decide whether the whole
  list must be rewritten.

> When an empty list is passed to the list `save`, the method returns immediately and **does not** clear
> existing rows for that key.

### 3.1 Entity

Required unique index: `(userId, sessionId, stateKey, itemIndex)` — see the example in section 2.

### 3.2 Builder

```java
AgentStateStore store = ArmyAgentStateStore.builder(MyAgentSession_.T)
        .sessionFactory(sessionFactory)                    // required
        .anonymousUserIdSupplier(() -> "anonymous")        // required
        .nullMode(NullMode.DEFAULT)                        // optional
        .literalMode(LiteralMode.DEFAULT)                  // optional
        .build();
```

`supportsVersioning()` returns `true`.

### 3.3 Usage

```java
public record AgentStatusState(String status, int step) implements State {
}

// single value: full replacement via dialect upsert
store.save("alice", "session-1", "agent_state", new AgentStatusState("running", 3));
Optional<AgentStatusState> loaded = store.get("alice", "session-1", "agent_state", AgentStatusState.class);

// optimistic locking
VersionedState<AgentStatusState> current =
        store.getVersioned("alice", "session-1", "agent_state", AgentStatusState.class);
long newVersion = store.saveIfVersion("alice", "session-1", "agent_state",
        new AgentStatusState("done", current.value().step()), current.version());
if (newVersion == AgentStateStore.UNVERSIONED) {
    // version conflict: nothing was written
}

// list
store.save("alice", "session-1", "history", List.of(
        new AgentStatusState("step-1", 1), new AgentStatusState("step-2", 2)));
List<AgentStatusState> history = store.getList("alice", "session-1", "history", AgentStatusState.class);

// session-level operations
boolean exists = store.exists("alice", "session-1");
Set<String> sessions = store.listSessionIds("alice");   // null lists the anonymous namespace
store.delete("alice", "session-1", "agent_state");      // delete one key
store.delete("alice", "session-1");                     // delete the whole session
```

The three `expectedVersion` cases of `saveIfVersion`:

| Value | Behavior |
|---|---|
| `0` | Create if absent; if present with version `0`, upgrade; if present with a non-zero version, conflict |
| `AgentStateStore.UNVERSIONED` (`-1`) | Unconditional overwrite |
| Any other value | Write only when the stored version equals this value |

On success the new version is returned; on conflict `UNVERSIONED` is returned and nothing is written.
When the row does not exist, `getVersioned` returns `new VersionedState<>(null, 0L)`.

---

## 4. BaseStore (package `store`)

Implements `io.agentscope.harness.agent.filesystem.remote.store.BaseStore`: a namespace-addressed key/value
store with optimistic versioning.

Columns of `AgentScopeStore`:

| Column | Type | Description |
|---|---|---|
| `namespacePath` | `String` | Namespace segments joined with the unit separator `0x1F`, trailing separator included; max 512 chars |
| `itemKey` | `String` | Item key within the namespace; max 255 chars |
| `valueJson` | `String` | Value as JSON, mapped to `io.army.mapping.PreferredJsonbType` |
| `version` | `Long` | CAS counter, managed by Army |

Plus `id`, `createTime`, `updateTime`.

### 4.1 Entity

```java
@Table(
        name = "my_remote_store",
        indexes = @Index(
                name = "${DEFAULT_VALUE}",
                unique = true,
                fieldList = {"namespacePath", "itemKey"}
        ),
        comment = "Remote store item"
)
public class MyStoreItem extends AgentScopeStore {
}
```

Required unique index: `(namespacePath, itemKey)`.

### 4.2 Builder

```java
BaseStore store = ArmyBaseStore.builder(MyStoreItem_.T)
        .sessionFactory(sessionFactory)   // required
        .nullMode(NullMode.DEFAULT)       // optional
        .literalMode(LiteralMode.DEFAULT) // optional
        .build();
```

### 4.3 Usage

```java
List<String> namespace = List.of("agent-1", "memory");

store.put(namespace, "profile", Map.of("name", "alice"));
StoreItem item = store.get(namespace, "profile");          // null when absent

boolean created = store.putIfVersion(namespace, "profile", Map.of("name", "bob"), 0);
boolean swapped = store.putIfVersion(namespace, "profile", Map.of("name", "carol"), item.version());

// prefix scan over sub-namespaces, ordered by itemKey
List<StoreItem> page = store.search(namespace, 50, 0);

store.delete(namespace, "profile");
```

Semantics worth knowing:

- `put` is a full upsert: PostgreSQL `ON CONFLICT (namespacePath, itemKey) DO UPDATE`, MySQL
  `ON DUPLICATE KEY UPDATE`.
- `putIfVersion` with `expectedVersion == 0` means create-if-absent (PostgreSQL `ON CONFLICT DO NOTHING`;
  MySQL relies on the duplicate-key error, sqlState `23000`). Any positive value is a CAS update
  (`WHERE version = expectedVersion`). Negative values are rejected.
- `search(namespace, limit, offset)` matches `namespacePath LIKE 'ns\u001F%'`, so it returns items of the
  namespace **and all its sub-namespaces**, ordered by `itemKey`. `limit <= 0` returns an empty list.
- Namespace segments must not be null, must not contain the unit separator `0x1F`, and the encoded path must
  not exceed 512 characters; keys must not be empty and must not exceed 255 characters.

---

## 5. RemoteSnapshotClient (package `snapshot`)

Implements `io.agentscope.harness.agent.sandbox.snapshot.RemoteSnapshotClient`: stores sandbox snapshot
archives as blobs.

Columns of `AgentScopeSnapshot`:

| Column | Type | Description |
|---|---|---|
| `id` | `String` | **The snapshot ID itself** — no generator is declared; `upload` assigns it |
| `data` | `byte[]` | Snapshot tar archive, mapped to `io.army.mapping.LongBlobType` (`bytea` / `LONGBLOB`) |
| `createTime` / `updateTime` | `LocalDateTime` | Army reserved |
| `version` | `Long` | Army reserved |

### 5.1 Entity

No extra unique index is needed — the primary key is the snapshot ID:

```java
@Table(name = "my_sandbox_snapshot", comment = "Sandbox snapshot archive")
public class MySnapshot extends AgentScopeSnapshot {
}
```

### 5.2 Builder

```java
RemoteSnapshotClient client = ArmyRemoteSnapshotClient.builder(MySnapshot_.T)
        .sessionFactory(sessionFactory)   // required
        .build();
```

### 5.3 Usage

```java
client.upload("snapshot-42", inputStream);     // upsert: re-upload replaces the archive atomically
boolean exists = client.exists("snapshot-42");
InputStream in = client.download("snapshot-42");
```

- `upload` reads the whole stream into memory, then performs a dialect upsert
  (PostgreSQL `ON CONFLICT (id) DO UPDATE SET data`, MySQL `ON DUPLICATE KEY UPDATE`).
- `download` throws `RuntimeException("Snapshot <id> not found")` when the ID is absent.

---

## 6. SandboxExecutionGuard (package `sandbox`)

Implements `io.agentscope.harness.agent.sandbox.SandboxExecutionGuard` with the database's **native lock
facility** — no table and no entity required:

| Dialect | Acquire | Release | Lock key |
|---|---|---|---|
| PostgreSQL | `pg_try_advisory_lock` | `pg_advisory_unlock` | 64-bit MurmurHash3 (128-bit hash, first long) of `keyPrefix + scope + ":" + value` |
| MySQL | `GET_LOCK(name, timeout)` | `RELEASE_LOCK(name)` | `keyPrefix + scope + ":" + value`, max 64 chars (longer names are truncated and suffixed with a hash) |

Both functions are issued as Army dialect expressions (`Postgres.pgTryAdvisoryLock` /
`MySQLs.getLock` / `MySQLs.releaseLock`) inside a `SELECT`.

```java
SandboxExecutionGuard guard = ArmySandboxExecutionGuard.builder()
        .sessionFactory(sessionFactory)          // required
        .keyPrefix("myapp:sandbox:lock:")        // optional, default "agentscope:sandbox:lock:"
        .lockTimeoutSeconds(600)                 // optional, default 1800 (30 minutes)
        .build();

try (SandboxLease lease = guard.tryEnter(isolationKey)) {
    // exclusive section for this isolation key
}
```

Behavior:

- `tryEnter` polls until the lock is acquired or `lockTimeoutSeconds` elapses; on timeout it throws
  `InterruptedException`.
- The guard holds one `SyncSession` for the whole lease lifetime, because both lock facilities are bound to
  the database session. `lease.close()` issues the unlock statement and closes the session; closing twice is
  safe.
- Only PostgreSQL and MySQL are supported; other dialects throw `UnsupportedOperationException`.

---

## 7. TableMeta.properties

File location:

```
src/main/resources/META-INF/army/TableMeta.properties
```

Army consults this file only when an annotation value is a placeholder:

| Annotation value | Behavior |
|---|---|
| `${DEFAULT}` | Use the configured value; otherwise fall back to the built-in default |
| `${RUNTIME}` | A configured value is mandatory; otherwise `MetaException` |
| `${OPTIONAL}` | Skip the element when no value is configured (supported by some attributes only) |
| Literal text | Used as-is; a property with the same key is **not** applied |

Key format:

```
{fully.qualified.EntityClass}.{fieldName}.{AnnotationName}.{attributeName}
{fully.qualified.EntityClass}.IndexMeta[0].name        # indexes use an array index
```

Frequently used keys:

| Key | Purpose | Default |
|---|---|---|
| `{FQCN}.Table.name` | Table name | `${DEFAULT}` converts the class name to snake_case |
| `{FQCN}.Table.ddlMode` | Whether to generate `CREATE TABLE` | `true` when absent |
| `{FQCN}.{field}.Column.name` | Column name | snake_case |
| `{FQCN}.{field}.Column.precision` | Length / precision | String 255, Decimal 16, UUID 36, Vector 1024 |
| `{FQCN}.{field}.Column.defaultValue` | Default value | Type-appropriate zero value |
| `{FQCN}.{field}.Mapping.value` | Fully-qualified mapping type class | Inferred from the Java type |
| `{FQCN}.{field}.GeneratorStrategy` | `ClassName[:JSON params]` | `id` falls back to `POST` (database auto-increment) |
| `{FQCN}.IndexMeta[i].name` | Index name | `${DEFAULT_VALUE}` derives it by convention |

### 7.1 ID generator

`AgentScopeSessions.id` and `AgentScopeStore.id` are `String` with `@Generator(type = GeneratorType.DEFAULT)`.
Without configuration Army falls back to `POST` (database-generated), which relational databases normally do
not provide for varchar primary keys — configure Snowflake8 explicitly:

```properties
# startTime is a millisecond timestamp; use a different value per table in the same database
my.pkg.MyAgentSession.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779290105917}
my.pkg.MyStoreItem.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779290105918}
```

`AgentScopeSnapshot.id` is the snapshot ID assigned by `upload`, so it needs **no** generator configuration.

### 7.2 A reference example

The stock example's
[TableMeta.properties](../army-test-example/army-example-stock-data/src/main/resources/META-INF/army/TableMeta.properties):

```properties
io.army.example.stock.domain.StockChatMemory.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779103273327}
io.army.example.stock.domain.StockChatMemory.id.Mapping.value=io.army.mapping.SqlBigIntType
io.army.example.stock.domain.StockChatVectorStore.embedding.Column.precision=1024
```

Follow the same format, but note that the stock example uses a numeric primary key (`SqlBigIntType`) — this
module's base classes use a `String` `id`, so do **not** copy that line.

---

## 8. Spring Boot wiring

Set up the session factory as usual — this mirrors the
[stock example's DataSourceConfiguration](../army-test-example/army-example-stock-data/src/main/java/io/army/example/stock/config/DataSourceConfiguration.java).
All builders in this module take the `SyncSessionFactory` directly:

```java
@Configuration
public class AgentScopeConfiguration {

    @Bean
    public FieldGeneratorFactory fieldGeneratorFactory() {
        return StandaloneFieldGeneratorFactory.getInstance();
    }

    @Bean
    public ArmySyncSessionFactoryBean armySyncSessionFactory(DataSource dataSource,
                                                             FieldGeneratorFactory fieldGeneratorFactory) {
        ArmySyncSessionFactoryBean factoryBean = ArmySyncSessionFactoryBean.create();
        factoryBean
                .setFactoryName("army-agentscope")
                .setDataSource(dataSource)
                .setPackagesToScan(List.of("my.pkg"))   // replace with your entity package
                .setFieldGeneratorFactory(fieldGeneratorFactory);
        return factoryBean;
    }

    @Bean
    public AgentStateStore agentStateStore(SyncSessionFactory sessionFactory) {
        return ArmyAgentStateStore.builder(MyAgentSession_.T)
                .sessionFactory(sessionFactory)
                .anonymousUserIdSupplier(() -> "anonymous")
                .build();
    }

    @Bean
    public BaseStore baseStore(SyncSessionFactory sessionFactory) {
        return ArmyBaseStore.builder(MyStoreItem_.T)
                .sessionFactory(sessionFactory)
                .build();
    }

    @Bean
    public RemoteSnapshotClient remoteSnapshotClient(SyncSessionFactory sessionFactory) {
        return ArmyRemoteSnapshotClient.builder(MySnapshot_.T)
                .sessionFactory(sessionFactory)
                .build();
    }

    @Bean
    public SandboxExecutionGuard sandboxExecutionGuard(SyncSessionFactory sessionFactory) {
        return ArmySandboxExecutionGuard.builder()
                .sessionFactory(sessionFactory)
                .build();
    }
}
```

> `setPackagesToScan` must include the package containing your entities, otherwise Army will not discover the
> `@Table` classes.

---

## 9. Validation and limits

Possible exceptions:

| Message | Cause | Fix |
|---|---|---|
| `sessionContext is required` | `ArmyAgentStateStore`: no session factory passed | Call `.sessionFactory(...)` |
| `sessionFactory is required` | No session factory passed to the builder | Call `.sessionFactory(...)` |
| `tableMeta is required` | The metamodel argument was `null` | Pass `MyEntity_.T` |
| `anonymousUserIdSupplier is required` | `ArmyAgentStateStore`: no anonymous ID supplier | Call `.anonymousUserIdSupplier(...)` |
| `... not exists index[unique=true] thant have N fields: [...]` | `@Index` does not match the required unique index | Fix `@Table` (sections 3.1 / 4.1) |
| `AgentStateStore userId cannot be null or empty` | A blank ID was passed | Pass a non-blank ID |
| `AgentStateStore sessionId cannot exceed 255 characters` | The value exceeds the field precision | Configure `{FQCN}.sessionId.Column.precision` |
| `key must not be empty` / `key length must not exceed 255` | `BaseStore` key validation | Shorten the key |
| `namespace path length must not exceed 512` | `BaseStore` namespace validation | Shorten the namespace |
| `Snapshot <id> not found` | `download` / missing snapshot ID | Check the ID with `exists` first |
| `... is supported` / `Unsupported database: ...` | A dialect other than MySQL/PostgreSQL | Use a supported database |

Additional notes:

- ID length validation in `ArmyAgentStateStore` applies only to string mappings, detected via
  `field.mappingType() instanceof MappingType.SqlString` with `precision > 0`.
- `stateValue` / `valueJson` are mapped with the literal `@Mapping("io.army.mapping.PreferredJsonbType")` and
  `data` with the literal `@Mapping("io.army.mapping.LongBlobType")`. Because these are not `${DEFAULT}`
  placeholders, `TableMeta.properties` cannot override them.
- If the table name is a literal (e.g. `name = "my_agent_session"`), the `Table.name` property has no effect.

---

## 10. Module relationships

```
agentscope-core      (AgentStateStore / State / VersionedState)
agentscope-harness   (BaseStore / RemoteSnapshotClient / SandboxExecutionGuard)  [optional]
       +
army-sync  ──────> army-agentscope
       +
army-postgre / army-mysql  (optional dialects)
```

Related documentation:

- [army-spring-ai-model-chat-memory](../army-spring-ai-model-chat-memory/README.md): another module that persists agent data
- [army-example-stock-data](../army-test-example/army-example-stock-data/README.md): complete entity, DAO, and configuration example
- [Army session management skill](../.trae/skills/army-session-management/SKILL.md): session factory and transaction configuration
