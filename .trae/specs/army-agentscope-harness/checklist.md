# Checklist — army-agentscope-agentstate Harness 三个扩展接口

## 依赖与构建

- [x] `army-agentscope-agentstate/pom.xml` 声明 `io.agentscope:agentscope-harness`
- [x] 模块 POM 配置 `ArmyMetaModelDomainProcessor`，clean 编译生成 2 个 MetaModel
- [x] `mvn -q -pl army-agentscope-agentstate -am compile -DskipTests` 通过
- [x] `mvn -q clean -pl army-agentscope-agentstate -am compile -DskipTests` 通过

## BaseStore

- [x] `AgentScopeDomain` 提供 Army 保留字段 `id` / `createTime` / `updateTime`，`id` 用 Snowflake8
- [x] `AgentScopeStoreItem` 唯一索引为 `(namespaceHash, itemKeyHash)`
- [x] `namespacePath` 精度 512、`itemKey` 精度 255、`valueJson` 使用 `PreferredJsonbType`
- [x] `version` 复用 Army 保留字段，由框架自动递增
- [x] `ArmyBaseStore` 实现 `get` / `put` / `putIfVersion` / `search` / `delete`
- [x] 对外 `StoreItem.version` 为 DB version + 1，`putIfVersion(expected>0)` 映射 DB version = expected - 1
- [x] `putIfVersion(0)` 为 create-if-absent
- [x] `put` 最多 8 次 CAS 重试，失败抛 `IllegalStateException`
- [x] `search` 使用 ESCAPE '!'、`orderBy(itemKey)`、`limit/offset`
- [x] PostgreSQL `ON CONFLICT DO NOTHING`；MySQL 捕获 sqlState 23000
- [x] 无原生 SQL

## SandboxSnapshotSpec

- [x] `AgentScopeSnapshot` 唯一索引 `snapshotId`，`data` 使用 `LongBlobType`
- [x] `ArmyRemoteSnapshotClient` 实现 `upload` / `download` / `exists`
- [x] `upload` 按方言 upsert（PG `ON CONFLICT DO UPDATE`、MySQL `ON DUPLICATE KEY UPDATE`）
- [x] `download` 未命中抛 `FileNotFoundException`，BLOB 通过字段 `mappingType()` 读取
- [x] `ArmySandboxSnapshotSpec` 继承 `RemoteSnapshotSpec`，产物类型为 `remote`
- [x] 无原生 SQL

## SandboxExecutionGuard

- [x] `builder(SyncSessionContext, DataSource)`，构建时按方言校验，非 PG/MySQL 抛异常
- [x] `keyPrefix` 为空抛 `IllegalArgumentException`，自动补尾部 `:`
- [x] `lockTimeout` 非正抛 `IllegalArgumentException`
- [x] PostgreSQL 使用 `pg_try_advisory_lock` 轮询 + deadline
- [x] 超时只关闭连接并抛 `InterruptedException`，不错误设置线程中断标记
- [x] MySQL 使用 `GET_LOCK` / `RELEASE_LOCK`，锁名超过 64 字符降级为 SHA-256
- [x] Lease `close()` 幂等，释放失败仅记录日志，最终关闭连接
- [x] 仅 Guard 使用原生 JDBC，且按方言分支

## 回归与风格

- [x] 未修改 `ArmyAgentStateStore` 与 `AgentScopeSessions`
- [x] 单 return、final 先声明后赋值、分支/循环均带花括号
- [x] 类级文档使用 `///`
- [x] 未新增 `META-INF/army/TableMeta.properties`
- [x] 未新增 Maven 模块，未改根 POM 的 `<modules>`
