# 在 army-agentscope-agentstate 中实现 AgentScope Harness 三个扩展接口

## 1. 目标与用户决策

在现有模块 `army-agentscope-agentstate` 内，为以下三个接口增加 Army 实现：

1. `io.agentscope.harness.agent.filesystem.remote.store.BaseStore`
2. `io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec`
3. `io.agentscope.harness.agent.sandbox.SandboxExecutionGuard`

已确认的三项决策：

| 决策点 | 选择 |
|---|---|
| 代码落点 | `army-agentscope-agentstate` 模块内适合的包，不新建 Maven 模块 |
| Guard 后端 | 按方言使用原生锁：PostgreSQL advisory lock、MySQL `GET_LOCK` |
| BaseStore 版本列 | 复用 Army 保留字段 `version`，对外版本号做 1 基映射 |

代码风格同时对齐现有 `ArmyAgentStateStore` 与
`/Users/zoro/repositories/java/hub/coder/coder-agent/src/main/resources/prompts/systemPrompt.md`：
final 先声明后赋值、单 return、分支/循环必须带花括号、Javadoc 用 `///`。

## 2. 当前项目状态（已核对）

- `army-agentscope-agentstate` 现有文件只有：
  `AgentScopeSessions.java`、`ArmyAgentStateStore.java`、`README.md`、`pom.xml`。
- `AgentScopeSessions.java` 处于修改状态（`itemIndex` 增加 `updatable = false`），
  本任务**不修改**该文件。
- 模块 `pom.xml` 尚**未**声明 `agentscope-harness`，需要新增。
- 根 `pom.xml` 已通过 `dependencyManagement` 管理 `io.agentscope:agentscope-harness:2.0.4`，
  `<modules>` 不需要改动。
- 本机 `~/.m2/repository/io/agentscope/agentscope-harness/2.0.4` 已存在。
- 模块内没有 `META-INF/army/TableMeta.properties`，本任务也不新增该文件。

## 3. 依赖变更

只修改 `army-agentscope-agentstate/pom.xml`，新增：

```xml
<dependency>
    <groupId>io.agentscope</groupId>
    <artifactId>agentscope-harness</artifactId>
</dependency>
```

根 `pom.xml` 已管理 `io.agentscope:agentscope-harness:2.0.4`，无需改根 POM，也不新增模块。
`agentscope-core` 保留现有依赖即可（`agentscope-harness` 会传递依赖它）。

已验证本机存在 `~/.m2/repository/io/agentscope/agentscope-harness/2.0.4`。

## 3. 包与文件清单

```text
army-agentscope-agentstate/src/main/java/io/army/agentscope/
├── domain/AgentScopeDomain.java
├── store/AgentScopeStoreItem.java
├── store/ArmyBaseStore.java
├── snapshot/AgentScopeSnapshot.java
├── snapshot/ArmyRemoteSnapshotClient.java
├── snapshot/ArmySandboxSnapshotSpec.java
└── sandbox/ArmySandboxExecutionGuard.java
```

不新增 `TableMeta.properties`：三个实体的 `id` 直接在注解上配置 Snowflake8，
避免与使用方自己的 `META-INF/army/TableMeta.properties` 发生同名资源覆盖。

## 4. 公共实体基类

`io.army.agentscope.domain.AgentScopeDomain`：

```java
@MappedSuperclass
public abstract class AgentScopeDomain {

    @Generator(
            value = "io.army.generator.snowflake.Snowflake8Generator",
            params = @Param(name = Snowflake8Generator.START_TIME, value = "1779290105917")
    )
    @Column(name = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP)
    private String id;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;

    // 链式 getter/setter，返回 AgentScopeDomain
}
```

- `id` 是 Army 保留字段，PRECEDE 阶段由 Snowflake8 生成。
- `createTime` / `updateTime` 由 Army 维护。
- `version` 只在 BaseStore 实体中声明；Snapshot 不需要乐观锁。

## 5. BaseStore 实现

### 5.1 实体 `store/AgentScopeStoreItem`

```java
@Table(
        name = "army_agentscope_store",
        indexes = @Index(
                name = "${DEFAULT_VALUE}",
                unique = true,
                fieldList = {"namespaceHash", "itemKeyHash"}
        ),
        comment = "AgentScope remote store item"
)
public class AgentScopeStoreItem extends AgentScopeDomain {

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = 64)
    private String namespaceHash;   // SHA-256(namespacePath)

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = 512)
    private String namespacePath;   // 0x1F 连接，末尾保留分隔符

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = 64)
    private String itemKeyHash;     // SHA-256(itemKey)

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = 255)
    private String itemKey;

    @Column(name = "${DEFAULT}", notNull = true)
    @Mapping("io.army.mapping.PreferredJsonbType")
    private String valueJson;

    @Column
    private Long version;           // 复用 Army 保留版本字段
}
```

数据库适配理由：

- 唯一索引改用 `(namespaceHash, itemKeyHash)`，而不是直接索引 512+255 字符的两列：
  - MySQL `utf8mb4` 下 512×4 + 255×4 = 3068 字节，已接近 3072 字节上限；
  - 使用 64 字符哈希后索引只有 64×4 + 64×4 = 512 字节；
  - 哈希同时保证 MySQL 默认大小写不敏感排序规则下 key 仍按大小写敏感区分，
    与参考 MySQL 实现的 `utf8mb4_bin` 语义一致。
- `namespacePath` 仅用于 `LIKE` 前缀检索，不进入索引，长度上限 512 与参考 MySQL 实现一致。
- `valueJson` 直接复用现有模块的 `PreferredJsonbType`：PG 映射 JSONB，MySQL 映射 JSON。

### 5.2 实现 `store/ArmyBaseStore`

```java
public final class ArmyBaseStore implements BaseStore {

    public static Builder builder(SyncSessionContext sessionContext);

    public static final class Builder {
        public Builder tableMeta(SimpleTableMeta<AgentScopeStoreItem> tableMeta);
        public ArmyBaseStore build();
    }
}
```

- `Builder.build()` 校验 `sessionContext`、`tableMeta` 非空；默认 `AgentScopeStoreItem_.T`。
- 构造时通过 `ObjectAccessorFactory.pojoConstructor(...)` 获取实体构造器，
  并缓存 `id / version / namespaceHash / namespacePath / itemKeyHash / itemKey / valueJson` 字段。
- `agentscope-harness:2.0.4` 的 `BaseStore` 没有 `supportsAtomicSessionStorage()`，
  因此不覆写该方法；版本能力由 `putIfVersion` 实现提供。
- MetaModel 由 `ArmyMetaModelDomainProcessor` 生成，模块 POM 必须显式配置
  `<annotationProcessors>`（与 `army-test-example` 各模块一致），否则 `AgentScopeStoreItem_` 不存在。

namespace 编码（与参考实现一致）：

- 用 `0x1F` 连接所有 segment，并在末尾保留一个 `0x1F`。
- segment 为空、包含 `0x1F`、编码后长度超过 512 时抛 `IllegalArgumentException`。
- key 非空且长度不超过 255，否则抛 `IllegalArgumentException`。

版本映射：

- DB 中 Army `version` 从 `0` 开始（`insertIfAbsent` 成功后即 0）。
- 对外 `StoreItem.version` = DB `version` + 1，满足接口“从 1 开始”的契约。
- `putIfVersion(expectedVersion)`：
  - `expectedVersion == 0`：create-if-absent；
  - `expectedVersion > 0`：条件更新 `WHERE version = expectedVersion - 1`。
- `expectedVersion < 0` 抛 `IllegalArgumentException`。

方法实现要点：

1. `get(namespace, key)`：
   - `SQLs.query().selects(s -> { s.selection(valueJson); s.selection(version); })`
   - `from(tableMeta, AS, "t")`
   - `where(namespaceHash.equal(hash)).and(itemKeyHash.equal(hash))`
   - `session.queryOneRecord(stmt, record -> new StoreItem(key,
     JSON.fromJson(record.getNonNull(0, String.class), MAP_TYPE),
     record.getNonNull(1, Long.class) + 1))`
   - 未命中返回 `null`。
   - `MAP_TYPE` 为 `new TypeReference<Map<String, Object>>() {}`。
2. `put(namespace, key, value)`：
   - 在 `executeVoid(name, false, ...)` 中执行最多 8 次 CAS 重试：
     1. 查询当前 DB `version`；
     2. 无行则 `insertIfAbsent`；
     3. 有行则条件更新 `valueJson` 且 `version = 当前 DB version`；
     4. 插入冲突或更新 0 行则重试；
     5. 超过次数抛 `IllegalStateException`。
   - PostgreSQL 插入：`Postgres.singleInsert().insertInto(tableMeta).as("t").values(list)
     .onConflict().parens(s -> s.space(namespaceHash).comma(itemKeyHash)).doNothing()`。
   - MySQL 插入：普通 `SQLs.singleInsert()`；捕获 `DriverException` 且
     `sqlState == "23000"` 视为冲突。
   - 更新：`SQLs.singleUpdate().update(tableMeta, AS, "t").set(valueJson, json)
     .where(namespaceHash.equal(...)).and(itemKeyHash.equal(...))
     .and(version.equal(dbVersion))`；捕获 `OptimisticLockException` 视为失败。
3. `putIfVersion(...)`：
   - `expectedVersion == 0` 调用 `insertIfAbsent`，返回是否插入成功；
   - 否则调用条件更新，`rowAffected == 1` 返回 `true`。
4. `search(namespace, limit, offset)`：
   - `limit <= 0` 返回 `List.of()`；`offset < 0` 归零。
   - 前缀 pattern = 转义后的 `namespacePath` + `%`，转义字符 `!`：
     `namespacePath.like(pattern, SQLs.ESCAPE, '!')`。
   - `orderBy(itemKey)` + `limit(SQLs::literal, offset, limit)`。
   - 每行映射为 `new StoreItem(itemKey, value, dbVersion + 1)`。
5. `delete(namespace, key)`：
   - `SQLs.singleDelete().deleteFrom(tableMeta, AS, "t")
     .where(namespaceHash.equal(...)).and(itemKeyHash.equal(...))`。

SHA-256 工具：`MessageDigest.getInstance("SHA-256")` + `HexFormat.of().formatHex(...)`，
异常包装为 `IllegalStateException`。

## 6. SandboxSnapshotSpec 实现

### 6.1 实体 `snapshot/AgentScopeSnapshot`

```java
@Table(
        name = "army_agentscope_snapshot",
        indexes = @Index(name = "${DEFAULT_VALUE}", unique = true, fieldList = {"snapshotId"}),
        comment = "AgentScope sandbox snapshot archive"
)
public class AgentScopeSnapshot extends AgentScopeDomain {

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = 512)
    private String snapshotId;

    @Column(name = "${DEFAULT}", notNull = true)
    @Mapping("io.army.mapping.LongBlobType")
    private byte[] data;
}
```

- `snapshotId` 唯一索引：512×4 = 2048 字节，满足 MySQL 索引长度限制。
- `LongBlobType` 在 PG 映射 `bytea`，在 MySQL 映射 `LONGBLOB`，由 Army 方言自动适配，
  不写死任一数据库类型。

### 6.2 客户端 `snapshot/ArmyRemoteSnapshotClient`

实现 `RemoteSnapshotClient`：

```java
public final class ArmyRemoteSnapshotClient implements RemoteSnapshotClient {
    public static Builder builder(SyncSessionContext sessionContext);
    void upload(String snapshotId, InputStream data) throws Exception;
    InputStream download(String snapshotId) throws Exception;
    boolean exists(String snapshotId) throws Exception;
}
```

- `upload`：
  - `byte[] bytes = data.readAllBytes()`；
  - 构造 `AgentScopeSnapshot`（snapshotId + data）；
  - PostgreSQL：`Postgres.singleInsert().insertInto(tableMeta).as("t").values(list)
    .onConflict().parens(s -> s.space(snapshotId)).doUpdate()
    .set(data, Postgres.excluded(data))`；
  - MySQL：`MySQLs.singleInsert().insertInto(tableMeta).values(list).as("r")
    .onDuplicateKey().update(data, SQLs.field("r", data))`；
  - 通过 `executeVoid(name, false, ...)` 执行。
- `download`：
  - `select(data).from(...).where(snapshotId.equal(id))`；
  - `session.queryOneRecord(stmt, record -> record.getNonNull(0, byte[].class, this.data.mappingType()))`；
  - 为 `null` 时抛 `FileNotFoundException("Snapshot not found: " + snapshotId)`；
  - 返回 `ByteArrayInputStream`。
- `exists`：
  - `select(id).from(...).where(snapshotId.equal(id)).limit(SQLs::literal, 1)`；
  - `session.queryOne(stmt, String.class) != null`。

### 6.3 Spec `snapshot/ArmySandboxSnapshotSpec`

```java
public final class ArmySandboxSnapshotSpec extends RemoteSnapshotSpec {

    public static Builder builder(SyncSessionContext sessionContext);

    public static final class Builder {
        public Builder tableMeta(SimpleTableMeta<AgentScopeSnapshot> tableMeta);
        public ArmySandboxSnapshotSpec build();
    }
}
```

- 构造时 `super(new ArmyRemoteSnapshotClient(sessionContext, tableMeta))`。
- 复用 `RemoteSandboxSnapshot`，因此 `getType()` 为 `"remote"`，`getId()` 为 snapshotId，
  `persist/restore/isRestorable` 行为与参考实现一致。

## 7. SandboxExecutionGuard 实现

### 7.1 公共 API `sandbox/ArmySandboxExecutionGuard`

```java
public final class ArmySandboxExecutionGuard implements SandboxExecutionGuard {

    public static Builder builder(SyncSessionContext sessionContext, DataSource dataSource);

    public SandboxLease tryEnter(SandboxIsolationKey key) throws InterruptedException;

    public static final class Builder {
        public Builder keyPrefix(String keyPrefix);
        public Builder lockTimeout(Duration timeout);
        public ArmySandboxExecutionGuard build();
    }
}
```

- 方言在 `build()` 时通过 `sessionContext.sessionFactory().dialectDatabase()` 确定：
  - `PostgreSQL` -> PostgreSQL advisory lock 后端；
  - `MySQL` -> MySQL `GET_LOCK` 后端；
  - 其它 -> `IllegalStateException`。
- `keyPrefix` 默认 `"agentscope:sandbox:lock:"`；为空抛 `IllegalArgumentException`；
  自动补尾部 `:`。
- `lockTimeout` 默认 30 分钟；必须为正。
- Guard 必须线程安全；每次 `tryEnter` 独立获取一个 `Connection`。

### 7.2 PostgreSQL 后端

- 锁 key：`SHA-256(keyPrefix + scope.name().toLowerCase() + ':' + value)` 取前 8 字节为 `long`。
- 获取：
  - `DataSource.getConnection()`；
  - `SELECT pg_try_advisory_lock(?)`（`setLong`）；
  - 成功 -> 返回 lease，lease 持有该 `Connection`；
  - 失败且未到 deadline -> `Thread.sleep(100)` 后重试；
  - 到期 -> 关闭连接并抛
    `InterruptedException("Timed out waiting for PostgreSQL advisory lock: " + key + ...)`；
  - 线程中断 -> 关闭连接并原样抛出；
  - `SQLException` -> 关闭连接并包为 `IllegalStateException`。
- 释放：`SELECT pg_advisory_unlock(?)`，然后关闭连接。
- 连接关闭时数据库自动释放 advisory lock，作为兜底。

### 7.3 MySQL 后端

- 锁名：`keyPrefix + scope.name().toLowerCase() + ':' + value`；
  超过 64 字符时使用 SHA-256 的 64 位十六进制串。
- 获取：
  - `DataSource.getConnection()`；
  - `SELECT GET_LOCK(?, ?)`，超时秒数取 `Math.max(1, lockTimeout.toSeconds())`；
  - 返回 1 -> lease；返回 0/NULL -> 关闭连接并抛 `InterruptedException`；
  - `SQLException` -> 关闭连接并包为 `IllegalStateException`。
- 释放：`SELECT RELEASE_LOCK(?)`，然后关闭连接。
- 连接关闭时 MySQL 自动释放命名锁，作为兜底。

### 7.4 Lease

- 私有静态内部类，持有 `Connection`、锁标识、后端类型。
- `close()` 使用 `AtomicBoolean` 保证幂等；释放失败只记录日志，不抛异常；
  最后一定关闭连接。
- 使用 `org.slf4j.Logger` 记录调试/告警日志。

### 7.5 使用约束（写入 Javadoc）

- `DataSource` 必须是连接池或独立数据源，不能是 Spring 事务绑定包装；
  Guard 会在 lease 生命周期内独占该连接。
- MySQL 命名锁是服务器级而非库级，多应用共享实例时应配置唯一 `keyPrefix`。

## 8. 与参考实现/现有模块的一致性

- BaseStore 的 namespace 编码、`StoreItem` 字段、`putIfVersion` 语义、`search` 分页
  与 `PostgresBaseStore` / `JdbcStore` 保持一致；差异仅在于用 Army Criteria API 和
  Army `version` 字段实现。
- Snapshot 通过 `RemoteSnapshotSpec` + `RemoteSnapshotClient` 复用 harness 的
  `RemoteSandboxSnapshot`，与 `PostgresSnapshotSpec` / `JdbcSnapshotSpec` 结构一致。
- Guard 的 PG/MySQL 原生锁行为与参考实现一致；用 SHA-256 替代参考实现依赖的
  `commons-codec` MurmurHash3，避免新增依赖。
- 不修改 `AgentScopeSessions` / `ArmyAgentStateStore` 现有逻辑。

## 9. 验收与验证

1. 依赖检查（已验证存在）：
   `ls ~/.m2/repository/io/agentscope/agentscope-harness/2.0.4`
2. 编译：
   `mvn -q -pl army-agentscope-agentstate -am compile -DskipTests`
3. clean 后重编，确认注解处理器生成 `AgentScopeStoreItem_`、`AgentScopeSnapshot_`：
   `mvn -q clean -pl army-agentscope-agentstate -am compile -DskipTests`
4. 自查：
   - 无原生 SQL 出现在 BaseStore/Snapshot 中；
   - 只有 Guard 使用原生 JDBC，且按方言分支；
   - 三个接口方法签名与 `agentscope-harness:2.0.4` 一致；
   - 未修改 `army-agentscope-agentstate` 的现有状态存储代码；
   - 单 return、final 先声明后赋值、循环/分支均带花括号。

## 10. 明确不做

- 不新建 Maven 模块，不改根 POM 的 `<modules>`。
- 不新增 `META-INF/army/TableMeta.properties`。
- 不为 BaseStore/Snapshot/Guard 增加无意义的 `userId` 字段：这三个接口没有用户维度；
  数据库适配体现在哈希索引、`LongBlobType`、方言原生锁。
- 不写 README（本任务未要求）。

## 11. 风险与规避

1. Army `version` 在 INSERT 时为 0：由注解文档保证 `version` 为 NOT NULL 保留字段，
   现有 `ArmyAgentStateStore.insertIfAbsent` 的成功路径也以 0 为初值。对外统一 +1。
2. MySQL `GET_LOCK` 调用不可被线程中断立即打断：与参考实现一致，超时由数据库侧控制；
   超时后抛 `InterruptedException`。
3. Snapshot `upload` 使用 `readAllBytes()` 全量载入内存：与参考实现一致；
   超大快照需调用方自行控制。
