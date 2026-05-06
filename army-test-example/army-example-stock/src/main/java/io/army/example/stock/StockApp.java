package io.army.example.stock;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class StockApp implements CommandLineRunner, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(StockApp.class);


    static void main(String[] args) {
        final SpringApplication app = new SpringApplication(StockApp.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }


    private final ChatClient chatClient;

    public StockApp(@Autowired ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        final var scanner = new Scanner(System.in);

        String input, response;
        Prompt prompt;
        SystemMessage sm;
        UserMessage um;
        AssistantMessage am;
        List<Message> messageList;
        final ChatClient chatClient = this.chatClient;
        System.out.println("我是机器人,你可以和我聊天.");
        while (true) {
            input = scanner.nextLine();
            if (input.trim().equals("exit")) {
                System.out.println("再见");
                break;
            }
            sm = new SystemMessage("你是deepseek");
            um = new UserMessage(input);
            am = new AssistantMessage("你是语法大师");
            messageList = List.of(
                    um
                    , sm
                    //  , am
            );
            prompt = new Prompt(messageList);
            response = chatClient.prompt(prompt).call().content();
            System.out.println(response);
        }

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
            LOG.info("api: {}", environment.getProperty("spring.ai.deepseek.api-key"));
        }
    }


}
