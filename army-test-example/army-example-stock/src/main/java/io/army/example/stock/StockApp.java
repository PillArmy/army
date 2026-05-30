package io.army.example.stock;


import org.springaicommunity.agent.tools.*;
import org.springaicommunity.agent.tools.task.TaskTool;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentType;
import org.springaicommunity.agent.utils.AgentEnvironment;
import org.springaicommunity.agent.utils.CommandLineQuestionHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootApplication(exclude = {OllamaChatAutoConfiguration.class})
public class StockApp {


    static void main(String[] args) {
        final SpringApplication app = new SpringApplication(StockApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }

    @Bean
    public CommandLineRunner demo(ChatClient.Builder chatClientBuilder,
                                  @Value("${BRAVE_API_KEY}") String braveApiKey,
                                  @Value("${agent.skills.paths}") List<Resource> skillPaths,
                                  @Value("classpath:/prompt/MAIN_AGENT_SYSTEM_PROMPT_V2.md") Resource agentSystemPrompt) {

        return args -> {
            // Configure Task tool with Claude sub-agents
            var taskTool = TaskTool.builder()
                    .subagentTypes(ClaudeSubagentType.builder()
                            .chatClientBuilder("default", chatClientBuilder.clone())
                            .skillsResources(skillPaths)
                            .braveApiKey(braveApiKey)
                            .build())
                    .build();

            ChatClient chatClient = chatClientBuilder
                    // Main agent prompt
                    .defaultSystem(p -> p.text(agentSystemPrompt) // system prompt
                            .param(AgentEnvironment.ENVIRONMENT_INFO_KEY, AgentEnvironment.info())
                            .param(AgentEnvironment.GIT_STATUS_KEY, AgentEnvironment.gitStatus())
                            .param(AgentEnvironment.AGENT_MODEL_KEY, "claude-sonnet-4-5-20250929")
                            .param(AgentEnvironment.AGENT_MODEL_KNOWLEDGE_CUTOFF_KEY, "2025-01-01"))

                    // Sub-Agents
                    .defaultTools(c -> {
                        c.callbacks(taskTool);
                        // Skills
                        c.callbacks(SkillsTool.builder()
                                .addSkillsResources(skillPaths)
                                .build());
                    })

                    // Core Tools
                    .defaultTools(
                            ShellTools.builder().build(),
                            FileSystemTools.builder().build(),
                            GrepTool.builder().build(),
                            GlobTool.builder().build(),
                            SmartWebFetchTool.builder(chatClientBuilder.clone().build()).build(),
                            BraveWebSearchTool.builder(braveApiKey).build())

                    // Task orchestration
                    .defaultTools(TodoWriteTool.builder().build())

                    // User feedback tool (use CommandLineQuestionHandler for CLI apps)
                    .defaultTools(AskUserQuestionTool.builder()
                            .questionHandler(new CommandLineQuestionHandler())
                            .build())

                    // Advisors
                    .defaultAdvisors(
                            ToolCallAdvisor.builder().conversationHistoryEnabled(false).build(), // Tool Calling
                            MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().maxMessages(500).build()).build()) // Memory

                    .build();

            String response = chatClient
                    .prompt("Search for Spring AI documentation and summarize it")
                    .call()
                    .content();
        };
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
