---
name: army-session-management
description: Army 框架会话管理完整指南。涵盖 SessionFactory 创建、SyncSession 执行（查询/更新/批处理/保存）、事务管理、Spring Boot 集成配置。Invoke when user needs to create sessions, execute statements, manage transactions, or configure Army with Spring Boot.
---

# Army Session Management

## 概述

Army 的会话管理分为三个层次：

1. **SessionFactory** — 会话工厂，持有数据库连接、元数据、配置
2. **Session** — 数据库会话，执行 Statement，管理事务
3. **Statement** — SQL 语句，通过 Criteria API 构建

```
FactoryBuilder → SessionFactory → Session → Statement 执行
```

---

## 一、SessionFactory 创建

### 1.1 FactoryBuilder API

`FactoryBuilder` 是创建 `SessionFactory` 的构建器接口（`io.army.session.FactoryBuilder`）。

**必填项**：

| 方法                             | 说明                                    |
|--------------------------------|---------------------------------------|
| `name(String)`                 | SessionFactory 名称，非空                  |
| `environment(ArmyEnvironment)` | Army 环境配置                             |
| `datasource(Object)`           | 数据源，支持 `ReadWriteSplittingDataSource` |
| `packagesToScan(List<String>)` | 扫描 domain 类的包路径列表                     |

**可选项**：

| 方法                                               | 说明               | 默认值                                              |
|--------------------------------------------------|------------------|--------------------------------------------------|
| `schema(String catalog, String schema)`          | catalog 和 schema | —                                                |
| `jsonCodec(JsonCodec)`                           | JSON 编解码器        | —                                                |
| `xmlCodec(XmlCodec)`                             | XML 编解码器         | —                                                |
| `factoryAdvice(Collection<FactoryAdvice>)`       | 工厂拦截器            | —                                                |
| `fieldGeneratorFactory(FieldGeneratorFactory)`   | 字段生成器工厂          | —                                                |
| `nameToDatabaseFunc(Function<String, Database>)` | 数据库名映射函数         | —                                                |
| `dataSourceOption(Option<T>, T)`                 | 数据源选项            | —                                                |
| `classLoader(ClassLoader)`                       | 类加载器             | `Thread.currentThread().getContextClassLoader()` |
| `loadStaticModel(boolean)`                       | 加载静态模型类          | `false`                                          |
| `validateOnStartup(boolean)`                     | 启动时校验            | `false`                                          |
| `tableMetaConsumer(Consumer<TableMeta<?>>)`      | TableMeta 遍历回调   | —                                                |
| `fieldMetaConsumer(Consumer<FieldMeta<?>>)`      | FieldMeta 遍历回调   | —                                                |

### 1.2 Spring Boot 自动配置

在 Spring Boot 中，`SessionFactory` 通常通过自动配置创建，无需手动调用 `FactoryBuilder`。

**核心配置属性**（`application.yml`）：

```yaml
army:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
  packages-to-scan:
    - com.example.domain
  schema:
    catalog: my_catalog
    schema: public
```

---

## 二、SyncSession — 阻塞式会话

### 2.1 接口定义

`SyncSession` extends `PackageSession`, `Closeable`。位于 `army-sync` 模块。

```java
// io.army.session.SyncSession
public sealed interface SyncSession extends PackageSession, Closeable
        permits SyncLocalSession, SyncRmSession, ArmySyncSession {
```

### 2.2 查询操作

| 方法                                                               | 返回          | 说明               |
|------------------------------------------------------------------|-------------|------------------|
| `queryOne(SimpleDqlStatement, Class<R>)`                         | `R`         | 查询单行，无结果抛异常      |
| `queryOneObject(SimpleDqlStatement, Supplier<R>)`                | `R`         | 通过构造器查询单行        |
| `queryOneRecord(SimpleDqlStatement, Function<CurrentRecord, R>)` | `R`         | 通过 Record 函数查询单行 |
| `queryList(DqlStatement, Class<R>)`                              | `List<R>`   | 查询多行到 List       |
| `queryObjectList(DqlStatement, Supplier<R>)`                     | `List<R>`   | 通过构造器查询多行        |
| `queryRecordList(DqlStatement, Function<CurrentRecord, R>)`      | `List<R>`   | 通过 Record 函数查询多行 |
| `query(DqlStatement, Class<R>)`                                  | `Stream<R>` | 流式查询             |
| `queryObject(DqlStatement, Supplier<R>)`                         | `Stream<R>` | 流式查询（构造器）        |
| `queryRecord(DqlStatement, Function<CurrentRecord, R>)`          | `Stream<R>` | 流式查询（Record 函数）  |

**查询示例**：

```java
// 查询单行 POJO
final Stock stock;
stock = session.queryOne(stmt, Stock.class);

// 查询多行 Map
final List<Map<String, Object>> list;
list = session.queryObjectList(stmt, RowMaps.hashMapConstructor());

// 流式查询
try (Stream<Stock> stream = session.query(stmt, Stock.class)) {
    stream.forEach(System.out::println);
}
```

### 2.3 更新操作

| 方法                                   | 返回             | 说明                             |
|--------------------------------------|----------------|--------------------------------|
| `update(SimpleDmlStatement)`         | `long`         | 执行 DML，返回影响行数                  |
| `updateAsStates(SimpleDmlStatement)` | `ResultStates` | 返回详细状态                         |
| `save(T domain)`                     | `int`          | 保存 domain 对象（insert or update） |
| `batchSave(List<T>)`                 | `int`          | 批量保存                           |
| `batchUpdate(BatchDmlStatement)`     | `List<Long>`   | 批量 DML                         |

**执行示例**：

```java
// 插入
final Insert stmt;
stmt = SQLs.singleInsert()
        .insertInto(Stock_.T)
        .values(stock)
        .asInsert();
final long rows = session.update(stmt);

// 更新
final Update stmt;
stmt = SQLs.singleUpdate()
        .update(Stock_.T, AS, "s")
        .set(Stock_.name, "New Name")
        .where(Stock_.id.equal(1L))
        .asUpdate();
session.update(stmt);

// 删除
final Delete stmt;
stmt = SQLs.singleDelete()
        .deleteFrom(Stock_.T, AS, "s")
        .where(Stock_.id.equal(1L))
        .asDelete();
session.update(stmt);

// 批量更新
final BatchUpdate stmt;
stmt = SQLs.batchSingleUpdate()
        .update(Stock_.T, AS, "s")
        .sets(pairs -> pairs
                .setSpace(Stock_.name, SQLs::namedParam)
                .setSpace(Stock_.offerPrice, SQLs::namedLiteral)
        )
        .where(Stock_.id.spaceEqual(SQLs::namedParam))
        .asUpdate()
        .namedParamList(paramList);
final List<Long> rowCounts = session.batchUpdate(stmt);
```

### 2.4 分页查询

```java
// PagingPair 包含 count 语句和 data 语句
final PagingPair pagingPair;
pagingPair = PagingPair.create(countStmt, dataStmt);

final Page<Stock> page;
page = session.paging(pagingPair, Stock.class, PageImpl::new);
```

### 2.5 Statement 选项

`SyncStmtOption` 提供对单次执行的细粒度控制：

```java
final SyncStmtOption option;
option = SyncStmtOption.builder()
        .timeoutMillis(5000)
        .build();

session.queryList(stmt, Stock.class, option);
session.update(stmt, option);
```

---

## 三、事务管理

### 3.1 基础事务 API

`SyncSession` 继承自 `Session`，事务方法来自 `SyncLocalSession` 子接口：

```java
// 开始事务
session.startTransaction(TransactionOption.option(Isolation.REPEATABLE_READ), HandleMode.AUTO);

// 提交
session.commit();

// 回滚
session.rollback();

// 标记只回滚
session.markRollbackOnly();

// 检查事务状态
session.inTransaction();       // 是否在事务块中
session.inPseudoTransaction();  // 是否在伪事务中
session.isRollbackOnly();       // 是否标记为只回滚
```

### 3.2 SavePoint

```java
final Object savePoint = session.setSavePoint();
// ... 操作 ...
session.rollbackToSavePoint(savePoint);
session.releaseSavePoint(savePoint);
```

### 3.3 只读与隔离级别

```java
// 只读会话
session.isReadonlySession();

// 只读状态
session.isReadOnlyStatus();

// 设置事务特征
session.setTransactionCharacteristics(
    TransactionOption.option(Isolation.REPEATABLE_READ)
);
```

---

## 四、Session 生命周期

### 4.1 Session 创建

```java
// 从 SessionFactory 创建
final SyncSessionFactory factory; // 从 Spring 注入
final SyncSession session;
session = factory.builder()
        .name("mySession")
        .readonly(false)
        .build();
```

### 4.2 Session 关闭

```java
// SyncSession 实现 Closeable
try (SyncSession session = factory.builder().build()) {
    // 执行操作
    session.update(stmt);
}
// session 自动关闭

// 或手动关闭
session.close();
```

### 4.3 Session 属性

```java
// 自定义属性（类似 HttpSession）
session.setAttribute("key", value);
final Object value = session.getAttribute("key");
session.removeAttribute("key");
```

---

## 五、SyncSessionContext — 共享会话上下文

`SyncSessionContext` 提供对 `SessionFactory` 级别共享资源的访问，常用于多组件之间共享会话：

```java
// 执行查询（只读）
final List<Stock> list;
list = sessionContext.executeNotNull("queryStocks", true, session -> {
    return session.queryList(stmt, Stock.class);
});

// 执行更新（读写）
sessionContext.executeVoid("updateStock", false, session -> {
    session.update(stmt);
});
```

### 5.1 方法签名

| 方法                                         | 返回                   | 说明        |
|--------------------------------------------|----------------------|-----------|
| `executeNotNull(name, readonly, Function)` | `<R> R`              | 执行回调，非空返回 |
| `executeVoid(name, readonly, Consumer)`    | `void`               | 执行回调，无返回  |
| `sessionFactory()`                         | `SyncSessionFactory` | 获取工厂      |

---

## 六、已知限制与注意事项

1. **visible 模式**：`Visible.ONLY_NON_VISIBLE` 或 `Visible.BOTH` 需要 Session 在白名单中，否则抛出 `VisibleModeException`
2. **只读会话**：只读 session 执行 DML 抛出 `ReadOnlySessionException`
3. **查询插入**：`queryInsert` 需要 session 允许 `queryInsert`
4. **子表 DML**：子表 UPDATE/DELETE 需要在事务中执行
5. **close**：Session 关闭后再使用抛出 `SessionException`

---

## Skill 进化机制

以下情况必须更新本 Skill：

1. 发现新的 Session API 或执行模式
2. 事务管理行为变更
3. Spring Boot 自动配置属性变更
4. 发现最佳实践或常见错误模式
