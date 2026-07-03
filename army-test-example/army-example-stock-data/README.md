## army-example-stock-data

Stock domain data layer module providing DAO and service implementations.

### Domain Models

- **Stock**: Stock basic information (id, exchange, code, name, status, etc.)
- **StockQuotes**: Stock price quotes
- **StockCandles**: Stock K-line candle data
- **StockRankDaily**: Daily stock ranking
- **StockUser**: Stock application user
- **StockChatConversation**: AI chat conversation
- **StockChatMemory**: Chat message history
- **StockChatVectorStore**: Vector store for chat embeddings
- **StockDocumentVectorStore**: Vector store for document embeddings
- **UploadRecord**: File upload records
- **VectorizedRecord**: Vectorized data records

### DAO Layer

- **StockBaseDao**: Base DAO interface extending `SyncBaseDao`
- **ArmyStockBaseDao**: Base DAO implementation using `SyncSessionContext`
- **StockChatConversationDao**: DAO for chat conversation operations
- **UploadRecordDao**: DAO for upload record operations

### Service Layer

- **StockBaseService**: Base service interface extending `SyncBaseService`
- **AbstractStockBaseService**: Abstract service using `TransactionTemplate`
- **StockChatConversationService**: Service for chat operations with read-only/write transaction handling
- **UploadRecordService**: Service for upload record operations

### Key Features

1. Compile-time metamodel generation with `@Table` annotation
2. Type-safe criteria queries using generated metamodel classes
3. Programmatic transaction management via `TransactionTemplate`
4. PostgreSQL-specific features (CTE, RETURNING, composite types)
5. Integration with Spring's dependency injection

### Configuration

- `DataSourceConfiguration`: Configures Army session factory and transaction manager
- `StockSessionFactoryAdvisor`: Session factory lifecycle management
- `TableMeta.properties`: Placeholder resolution for table metadata
- `Expressions.properties`: Custom SQL expressions
