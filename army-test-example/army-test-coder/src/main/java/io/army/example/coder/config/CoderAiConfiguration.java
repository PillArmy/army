package io.army.example.coder.config;


import io.army.example.coder.domain.CoderChatMemory;
import io.army.example.coder.domain.CoderChatMemory_;
import io.army.example.coder.domain.CoderChatVectorStore;
import io.army.example.coder.domain.CoderChatVectorStore_;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.ArmyMessageChatMemory;
import io.army.spring.ai.chat.memory.ArmyMessageChatMemoryAdvisor;
import io.army.spring.ai.chat.memory.SystemMessageAdvisor;
import io.army.spring.ai.vectorstore.ArmyVectorStore;
import io.army.spring.ai.vectorstore.ArmyVectorStoreChatMemoryAdvisor;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationConvention;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class CoderAiConfiguration implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;

    }


    @Bean
    public ArmyMessageChatMemory<CoderChatMemory> coderChatMemory(SyncSessionContext context) {
        return ArmyMessageChatMemory.builder(context, CoderChatMemory_.T)
                .maxMessages(30)
                .build();
    }


    @Bean
    public ArmyVectorStore<CoderChatVectorStore> chatVectorStore(EmbeddingModel embeddingModel, SyncSessionContext context) {
        return ArmyVectorStore.builder(embeddingModel, context, CoderChatVectorStore_.T)
                .build();
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatClientBuilderConfigurer chatClientBuilderConfigurer,
                                                @Qualifier("deepSeekChatModel") ChatModel chatModel,
                                                ObjectProvider<ObservationRegistry> observationRegistry,
                                                ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
                                                ObjectProvider<AdvisorObservationConvention> advisorObservationConvention) {
        ChatClient.Builder builder = ChatClient.builder(chatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                chatClientObservationConvention.getIfUnique(), advisorObservationConvention.getIfUnique());
        return chatClientBuilderConfigurer
                .configure(builder)
                .defaultTemplateRenderer(ArmyTemplateRenderer.defaultInstance());
    }

    @Bean
    public ChatClient coderChatClient(ChatClient.Builder builder, ArmyMessageChatMemory<CoderChatMemory> chatMemory,
                                      ArmyVectorStore<CoderChatVectorStore> vectorStore) {


        final List<Advisor> advisorList = List.of(
                SystemMessageAdvisor.of(Ordered.HIGHEST_PRECEDENCE + 10),
                ArmyVectorStoreChatMemoryAdvisor.builder(vectorStore).build(),
                ToolCallingAdvisor.builder().conversationHistoryEnabled(true).build(),
                ArmyMessageChatMemoryAdvisor.builder(chatMemory)
                        .order(Ordered.HIGHEST_PRECEDENCE + 500)
                        .build()
        );

        builder.defaultAdvisors(advisorList);

        AgentTool.createAgent(builder);

        builder.defaultTools(
                chatMemory.memoryTool(null),
                vectorStore.memoryTool(null)
        );

        return builder.defaultSystem(p -> p.text(AgentTool.loadSystemPrompt()) // system prompt
                        .param(AgentEnv.ENVIRONMENT_INFO, AgentEnv.info(this.environment))
                        .param(AgentEnv.GIT_STATUS, AgentEnv.gitStatus())
                        .param(AgentEnv.OFFSET_NOW, ArmyTemplateRenderer.OFFSET_NOW)
                        .param(AgentEnv.BIRTH_PERIOD, ArmyTemplateRenderer.BIRTH_PERIOD)
                        .param("EMPTY", "{}")
                )
                .build();
    }


}
