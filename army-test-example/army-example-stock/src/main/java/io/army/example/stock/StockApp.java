package io.army.example.stock;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

@SpringBootApplication
public class StockApp implements ApplicationRunner, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(StockApp.class);


    static void main(String[] args) {
        final SpringApplication app = new SpringApplication(StockApp.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }


    private final ChatClient chatClient;

    public StockApp(@Autowired ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        final var scanner = new Scanner(System.in);

        String input;
        Prompt prompt;
        SystemMessage sm;
        UserMessage um;
        AssistantMessage am;
        List<Message> messageList;
        final ChatClient chatClient = this.chatClient;

        final Path path = Path.of(System.getProperty("user.dir"),
                "army-test-example/army-example-stock/src/test/resources/my-local/console.md");

        try (var writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {

            final Consumer<String> writerConsumer = str -> {
                try {
                    writer.write(str);
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            System.out.println("我是资深股票分析师,你可以和我聊天.");

            while (true) {
                input = scanner.nextLine();
                if (input.trim().equals("exit")) {
                    System.out.println("再见");
                    break;
                }
                sm = new SystemMessage("你是资深股票分析师");
                um = new UserMessage(input);
                messageList = List.of(
                        um
                        , sm
                );
                prompt = new Prompt(messageList);

                writer.write("我: ");
                writer.write(input);
                writer.newLine();
                writer.newLine();

                writer.write("机器人: ");
                writer.flush();
                chatClient.prompt(prompt)
                        .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "8233"))
                        .stream()
                        .content()
                        .doOnNext(System.out::print)
                        .doOnNext(writerConsumer)
                        .blockLast();

                writer.newLine();
                writer.newLine();
                writer.flush();
                System.out.println();

            }
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
            // LOG.info("api: {}", environment.getProperty("spring.ai.deepseek.api-key"));
        }
    }


}
