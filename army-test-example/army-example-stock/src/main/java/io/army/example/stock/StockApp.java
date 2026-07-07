package io.army.example.stock;


import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.core.env.ConfigurableEnvironment;

/// Spring Boot application entry point for the **Stock AI Chat** web application.
///
/// <p>This application integrates:</p>
/// - **Army ORM** for PostgreSQL data persistence (stock data, chat conversations, vector stores)
/// - **Spring AI** for LLM-powered chat with tool calling and memory
/// - **Sub-agent architecture** using Claude-based task delegation
///
/// <p>Configured as a **servlet web application** with chat streaming, document upload,
/// and vector-based RAG (Retrieval-Augmented Generation) capabilities.</p>
///
/// ### Key Components
/// - `ChatClient` — Spring AI chat client with advisors (tool calling, memory)
/// - `TaskTool` — Sub-agent delegation for complex tasks
/// - `StockSessionFactoryAdvisor` — Data seeding on startup
///

@SpringBootApplication(exclude = {OllamaChatAutoConfiguration.class})
public class StockApp {


    static void main(String[] args) {
        final SpringApplication app = new SpringApplication(StockApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }



    public static final class StockSpringApplicationRunListener implements SpringApplicationRunListener {

        private final SpringApplication application;


        public StockSpringApplicationRunListener(SpringApplication application) {
            this.application = application;
        }


        @Override
        public void starting(ConfigurableBootstrapContext bootstrapContext) {
            if (this.application.getWebApplicationType() == WebApplicationType.NONE) {
                this.application.setKeepAlive(true);
            }
        }

        @Override
        public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
            // LOG.info("api: {}", environment.getProperty("spring.ai.deepseek.api-key"));
        }
    }


}
