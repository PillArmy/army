# Tasks — army-agentscope-agentstate 三个 Harness 扩展接口

> 状态标记：`[ ]` 待办，`[~]` 进行中，`[x]` 完成。

## T1 依赖
- [x] `army-agentscope-agentstate/pom.xml` 增加 `io.agentscope:agentscope-harness`
- [x] 确认 `agentscope-harness:2.0.4` 在本机仓库存在
- [x] 模块 POM 配置 `ArmyMetaModelDomainProcessor`（否则 MetaModel 不生成）
- [x] 验证：`mvn -q -pl army-agentscope-agentstate -am compile -DskipTests` 能解析依赖

## T2 公共实体基类
- [x] 新建 `io.army.agentscope.domain.AgentScopeDomain`
- [x] `id` 使用 `@Generator(value = Snowflake8Generator FQCN, params = @Param(START_TIME, ...))`
- [x] `createTime` / `updateTime`
- [x] 链式 getter/setter
- [x] 验证：编译通过，注解处理器识别 `id`

## T3 BaseStore
- [x] 新建 `store/AgentScopeStoreItem`
  - [x] 唯一索引 `{namespaceHash, itemKeyHash}`
  - [x] `namespacePath` precision 512、`itemKey` precision 255
  - [x] `valueJson` 使用 `PreferredJsonbType`
  - [x] `version` 复用 Army 保留字段
- [x] 新建 `store/ArmyBaseStore`
  - [x] 版本能力通过 `putIfVersion` 提供；harness 2.0.4 无 `supportsAtomicSessionStorage`，不覆写
  - [x] namespace 0x1F 编码与校验
  - [x] `get` 对外版本 DB+1
  - [x] `put` CAS 重试（最多 8 次）
  - [x] `putIfVersion` 0=创建，>0 映射 DB 版本 `expected-1`
  - [x] `search` LIKE + ESCAPE '!' + orderBy + limit/offset
  - [x] `delete`
  - [x] PG `ON CONFLICT DO NOTHING` / MySQL 23000 冲突处理
- [x] 验证：编译通过；逐方法对照 `BaseStore` 契约

## T4 Snapshot
- [x] 新建 `snapshot/AgentScopeSnapshot`
  - [x] 唯一索引 `{snapshotId}`
  - [x] `data` 使用 `LongBlobType`
- [x] 新建 `snapshot/ArmyRemoteSnapshotClient`
  - [x] `upload` 方言 upsert
  - [x] `download` 未命中抛 `FileNotFoundException`
  - [x] `exists`
- [x] 新建 `snapshot/ArmySandboxSnapshotSpec extends RemoteSnapshotSpec`
- [x] 验证：编译通过；`build(String)` 返回 `RemoteSandboxSnapshot`

## T5 SandboxExecutionGuard
- [x] 新建 `sandbox/ArmySandboxExecutionGuard`
  - [x] `builder(SyncSessionContext, DataSource)`
  - [x] `keyPrefix` / `lockTimeout` 校验
  - [x] 方言分派 PG / MySQL，其它抛 `IllegalStateException`
  - [x] PG：`pg_try_advisory_lock` 轮询 + deadline + `pg_advisory_unlock`
  - [x] MySQL：`GET_LOCK` / `RELEASE_LOCK` + 64 字符哈希降级
  - [x] SHA-256 派生 PG 64 位 key
  - [x] Lease `close` 幂等，异常仅记录日志
  - [x] 中断 / 超时路径关闭连接；超时不设置线程中断标记
- [x] 验证：编译通过；对照 `SandboxExecutionGuard` / `SandboxLease` 契约

## T6 编译与自查
- [x] `mvn -q -pl army-agentscope-agentstate -am compile -DskipTests`
- [x] `mvn -q clean -pl army-agentscope-agentstate -am compile -DskipTests`
- [x] 自查无原生 SQL 出现在 Store/Snapshot
- [x] 自查未修改现有 State 实现
- [x] 输出变更摘要与编译结果
