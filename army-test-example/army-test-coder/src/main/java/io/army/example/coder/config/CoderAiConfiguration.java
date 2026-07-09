package io.army.example.coder.config;


import io.army.example.coder.domain.CoderChatMemory_;
import io.army.example.coder.domain.CoderChatVectorStore_;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.ArmyMessageChatMemory;
import io.army.spring.ai.vectorstore.ArmyVectorStore;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationConvention;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ChatMemory coderChatMemory(SyncSessionContext context) {
        return ArmyMessageChatMemory.builder(context, CoderChatMemory_.T)
                .maxMessages(30)
                .build();
    }


    @Bean
    public VectorStore chatVectorStore(EmbeddingModel embeddingModel, SyncSessionContext context) {
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
    public ChatClient coderChatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                      @Qualifier("chatVectorStore") VectorStore vectorStore) {
        final List<Advisor> advisorList = List.of(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                VectorStoreChatMemoryAdvisor.builder(vectorStore).build(),
                ToolCallingAdvisor.builder().build()
        );

        builder.defaultAdvisors(advisorList);

        AgentTool.createAgent(builder, this.environment);

        return builder.build();
    }


}
