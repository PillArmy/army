package io.army.example.stock.config;

import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.ArmyChatMemoryRepository;
import io.army.spring.ai.vectorstore.ArmyVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfiguration {

    @Bean
    public ChatMemoryRepository stockChatMemoryRepository(SyncSessionContext sessionContext) {
        return ArmyChatMemoryRepository.create(sessionContext);
    }


    @Bean
    public VectorStore stockVectorStore(EmbeddingModel embeddingModel, SyncSessionContext context) {
        return ArmyVectorStore.builder(embeddingModel, context)
                .build();
    }


    @Bean
    public ChatClient stockChatClient(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        RetrievalAugmentationAdvisor.builder()
                .build();
        return builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultAdvisors()
                .build();
    }


}
