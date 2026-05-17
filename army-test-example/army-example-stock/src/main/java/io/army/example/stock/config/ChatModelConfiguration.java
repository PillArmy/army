package io.army.example.stock.config;

import io.army.criteria.LiteralMode;
import io.army.criteria.NullMode;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.ArmyMessageChatMemory;
import io.army.spring.ai.vectorstore.ArmyVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfiguration {


    @Bean
    public ChatMemory stockChatMemory(SyncSessionContext sessionContext) {
        return ArmyMessageChatMemory.builder(sessionContext)
                .nullMode(NullMode.DEFAULT)
                .literalMode(LiteralMode.LITERAL)
                .maxMessages(20)
                .build();
    }


    // @Bean
    public VectorStore stockVectorStore(EmbeddingModel embeddingModel, SyncSessionContext context) {
        return ArmyVectorStore.builder(embeddingModel, context)
                .build();
    }


    @Bean
    public ChatClient stockChatClient(ChatClient.Builder builder, @Qualifier("stockChatMemory") ChatMemory chatMemory) {
        return builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }


}
