package io.army.example.stock.config;

import io.army.example.stock.domain.StockChatMemory_;
import io.army.example.stock.domain.StockChatVectorStore_;
import io.army.example.stock.domain.StockDocumentVectorStore_;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.ArmyMessageChatMemory;
import io.army.spring.ai.vectorstore.ArmyVectorStore;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatModelConfiguration {


    @Bean
    public ChatMemory stockChatMemory(SyncSessionContext sessionContext) {
        return ArmyMessageChatMemory.builder(sessionContext, StockChatMemory_.T)
                .maxMessages(20)
                .build();
    }


    @Bean
    public VectorStore stockChatVectorStore(EmbeddingModel embeddingModel, SyncSessionContext context) {
        return ArmyVectorStore.builder(embeddingModel, context, StockChatVectorStore_.T)
                .build();
    }

    @Bean
    public VectorStore stockDocumentVectorStore(EmbeddingModel embeddingModel, SyncSessionContext context) {
        return ArmyVectorStore.builder(embeddingModel, context, StockDocumentVectorStore_.T)
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
        return chatClientBuilderConfigurer.configure(builder);
    }

    @Bean
    public ChatClient stockChatClient(ChatClient.Builder builder, @Qualifier("stockChatMemory") ChatMemory chatMemory,
                                      @Qualifier("stockChatVectorStore") VectorStore vectorStore) {
        final List<Advisor> advisorList = List.of(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
        );
        return builder.defaultAdvisors(advisorList)
                .build();
    }


}
