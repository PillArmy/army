package io.army.example.stock;


import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.core.env.ConfigurableEnvironment;

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
