## army-example-stock

Stock web application module with AI-powered chat functionality.

### Components

- **DocumentService**: Service for document processing and vectorization
- **ChatConversationController**: REST API controller for chat operations
- **Tools**: Custom tools for AI agent (stock data lookup, document search)
- **ChatModelConfiguration**: Spring AI chat model configuration
- **StockWebConfiguration**: Web MVC configuration

### Web Interface

- **index.html**: Main page with stock data visualization
- **chat.html**: AI chat interface for stock analysis
- **empty-chat.html**: Empty chat state page

### Key Features

1. **AI Chat**: Integration with Spring AI for conversational stock analysis
2. **Document Processing**: Upload and vectorize documents for RAG (Retrieval-Augmented Generation)
3. **REST API**: Chat conversation endpoints for frontend integration
4. **Cookie-based Authentication**: Simple session management

### Dependencies

- **army-example-stock-data**: Stock domain data layer
- **Spring AI**: AI chat model integration
- **Nacos**: Configuration management (optional)

### Application Entry

**StockApp.java**: Spring Boot application entry point
