## army-spring

This module provides Spring Framework integration for the Army database access framework.

### Main Packages

#### io.army.spring.sync

Spring integration for blocking (synchronous) sessions.

- **`ArmySyncSessionFactoryBean`**: Spring FactoryBean for creating `SyncSessionFactory`.
- **`ArmySyncLocalTransactionManager`**: Spring PlatformTransactionManager for Army local transactions.
- **`SyncBaseService`**: Base service interface with common CRUD operations.
- **`ArmySyncBaseService<D>`**: Abstract base service class with transaction support.
- **`SpringSyncSessionContext`**: Session context that integrates with Spring's ThreadLocal.

#### io.army.spring

Shared Spring integration utilities.

- **`ArmySessionFactoryBeanSupport`**: Base class for Army session factory beans.
- **`SpringArmyEnvironment`**: Army environment that reads from Spring's environment.
- **`TransactionTemplate`**: Template interface for programmatic transaction management.

### Key Features

1. **Spring Transaction Integration**: Integrates with Spring's transaction management.
2. **Session Factory Beans**: Easy configuration of Army session factories as Spring beans.
3. **DataSource Integration**: Works with Spring-managed DataSources (HikariCP, Druid).
4. **Programmatic Transactions**: Use `TransactionTemplate` for explicit transaction control.

---

## Service Layer

The service layer provides transactional data access through `TransactionTemplate`.

### Base Service Interface

```java
public interface SyncBaseService {
    <T> void save(T domain);

    <T> void batchSave(List<T> domainList);

    <T> T get(Class<T> domainClass, Object id);

    <T> T getByUnique(Class<T> domainClass, String fieldName, Object fieldValue);

    <T> boolean existsById(Class<T> domainClass, Object id);

    <T> long rowCount(Class<T> domainClass);

    <T> long updateField(Class<T> domainClass, Object id, String fieldName, Object fieldValue);
}
```

### Abstract Base Service Implementation

`ArmySyncBaseService<D>` implements `SyncBaseService` and provides transactional wrappers:

```java
public abstract class ArmySyncBaseService<D extends SyncBaseDao> implements SyncBaseService {

    protected final TransactionTemplate transactionTemplate;

    public ArmySyncBaseService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> void save(T domain) {
        this.transactionTemplate.executeWithoutResult(Isolation.READ_COMMITTED, false,
                _ -> getDao().save(domain)
        );
    }

    @Override
    public <T> T get(Class<T> domainClass, Object id) {
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, true,
                _ -> getDao().get(domainClass, id)
        );
    }

    protected abstract D getDao();
}
```

### Domain-Specific Service

Extend `ArmySyncBaseService` to implement domain-specific operations:

```java

@Service("stockChatConversationService")
public class StockChatConversationServiceImpl extends AbstractStockBaseService
        implements StockChatConversationService {

    private final StockChatConversationDao stockChatConversationDao;

    public StockChatConversationServiceImpl(TransactionTemplate transactionTemplate,
                                            StockChatConversationDao stockChatConversationDao) {
        super(transactionTemplate);
        this.stockChatConversationDao = stockChatConversationDao;
    }

    @Override
    public List<StockChatConversation> queryUserConversation(long userId) {
        return this.transactionTemplate.executeNoNull(true,
                _ -> this.stockChatConversationDao.queryUserConversation(userId)
        );
    }

    @Override
    public long deleteConversation(long userId, long conversationId) {
        return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.deleteConversation(userId, conversationId)
        );
    }

    @Override
    protected StockBaseDao getDao() {
        return this.stockChatConversationDao;
    }
}
```

**Key Points**:

- Read operations use `readOnly=true`
- Write operations use `Isolation.READ_COMMITTED, readOnly=false`
- `executeNoNull()` for methods that should never return null
- `execute()` for methods that may return null
- `executeWithoutResult()` for void methods

---

## DAO Layer

The DAO layer provides session-based data access.

### Base DAO Interface

```java
public interface SyncBaseDao {
    <T> void save(T domain);

    <T> void batchSave(List<T> domainList);

    <T> T get(Class<T> domainClass, Object id);
    // ... other CRUD methods
}
```

### Base DAO Implementation

```java

@Repository("armyStockBaseDao")
public class ArmyStockBaseDao extends ArmySyncBaseDao implements StockBaseDao {

    public ArmyStockBaseDao(SyncSessionContext sessionContext) {
        super(sessionContext);
    }
}
```

### Domain-Specific DAO

Extend the base DAO to implement domain-specific queries using Army's Criteria API:

```java

@Repository("stockChatConversationDao")
public class StockChatConversationDaoImpl extends ArmyStockBaseDao
        implements StockChatConversationDao {

    public StockChatConversationDaoImpl(SyncSessionContext sessionContext) {
        super(sessionContext);
    }

    @Override
    public List<StockChatConversation> queryUserConversation(long userId) {
        final Select stmt = SQLs.query()
                .select("t", PERIOD, StockChatConversation_.T)
                .from(StockChatConversation_.T, AS, "t")
                .where(StockChatConversation_.userId.equal(userId))
                .orderBy(StockChatConversation_.id.desc())
                .asQuery();
        return this.sessionContext.currentSession()
                .queryObjectList(stmt, StockChatConversation::new);
    }

    @Override
    public long deleteConversation(long userId, long conversationId) {
        final String w1 = "w1";
        final Delete stmt = Postgres.singleDelete()
                .with(w1).as(ws -> ws.deleteFrom(StockChatConversation_.T, AS, "t")
                        .where(StockChatConversation_.userId.equal(userId))
                        .and(StockChatConversation_.id.equal(conversationId))
                        .returning(StockChatConversation_.id)
                        .asReturningDelete()
                ).space()
                .deleteFrom(StockChatMemory_.T, AS, "t")
                .using(w1)
                .where(StockChatMemory_.conversationId.equal(refField(w1, StockChatConversation_.ID)))
                .asDelete();
        return this.sessionContext.currentSession().update(stmt);
    }
}
```

**Key Points**:

- Use `this.sessionContext.currentSession()` to get the current session
- Build queries using Army's Criteria API (`SQLs.query()`, `Postgres.singleDelete()`, etc.)
- Use generated metamodel classes (`StockChatConversation_`, `StockChatMemory_`) for type-safe field references
- Use `queryObjectList()` for list queries, `queryOne()` for single results, `update()` for write operations

---

## TransactionTemplate

The `TransactionTemplate` interface provides programmatic transaction management.

### Interface Methods

| Method                                                                           | Description                               |
|----------------------------------------------------------------------------------|-------------------------------------------|
| `execute(boolean readOnly, TransactionCallback<T>)`                              | Execute with read-only flag               |
| `execute(Isolation, boolean readOnly, TransactionCallback<T>)`                   | Execute with isolation level              |
| `execute(TransactionDefinition, TransactionCallback<T>)`                         | Execute with full definition              |
| `executeNoNull(boolean readOnly, TransactionCallback<T>)`                        | Execute, never return null                |
| `executeNoNull(Isolation, boolean readOnly, TransactionCallback<T>)`             | Execute with isolation, never return null |
| `executeWithoutResult(boolean readOnly, Consumer<TransactionStatus>)`            | Execute void operation                    |
| `executeWithoutResult(Isolation, boolean readOnly, Consumer<TransactionStatus>)` | Execute void with isolation               |

### Static Factory Methods

Create `TransactionDefinition` instances:

```java
TransactionDefinition def = TransactionTemplate.of(Isolation.READ_COMMITTED, false);
TransactionDefinition def = TransactionTemplate.of(Propagation.REQUIRED, Isolation.READ_COMMITTED, false);
TransactionDefinition def = TransactionTemplate.of(Propagation.REQUIRED, Isolation.READ_COMMITTED, false, 30);
```

### Usage Patterns

**Read Operation** (returns nullable):

```java
return this.transactionTemplate.execute(true,_ ->this.dao.

someQuery(param));
```

**Read Operation** (never null):

```java
return this.transactionTemplate.executeNoNull(true,_ ->this.dao.

someQuery(param));
```

**Write Operation**:

```java
return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,_ ->this.dao.

someUpdate(param));
```

**Void Operation**:

```java
this.transactionTemplate.executeWithoutResult(Isolation.READ_COMMITTED, false,_ ->this.dao.

someSave(entity));
```

---

## Spring Boot Configuration

```java

@Configuration
public class ArmyConfig {

    @Bean
    public SyncSessionFactory sessionFactory(DataSource dataSource) {
        return new ArmySyncSessionFactoryBean()
                .setDataSource(dataSource)
                .setDatabase(Database.POSTGRESQL)
                .afterPropertiesSet();
    }

    @Bean
    public PlatformTransactionManager transactionManager(SyncSessionFactory factory) {
        return new ArmySyncLocalTransactionManager(factory);
    }
}
```

### Dependencies

- **`army-core`**: Core Army types and criteria API.
- **`army-sync`**: Synchronous session API.
- **Spring Framework**: Spring Core, Spring Context, Spring Transaction.
