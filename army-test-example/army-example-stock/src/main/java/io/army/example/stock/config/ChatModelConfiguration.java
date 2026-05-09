package io.army.example.stock.config;

import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.ArmyChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfiguration {

    @Bean
    public ChatMemoryRepository stockChatMemoryRepository(SyncSessionContext sessionContext) {
        return ArmyChatMemoryRepository.create(sessionContext);
    }


    @Bean
    public ChatClient stockChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }


}
