package io.army.example.coder.tool;

import io.army.example.coder.domain.CoderChatMemory;
import io.army.example.coder.domain.CoderChatVectorStore;
import io.army.spring.ai.chat.memory.ArmyMessageChatMemory;
import io.army.spring.ai.vectorstore.ArmyVectorStore;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("memoryTool")
public class MemoryTool {

    private final ArmyMessageChatMemory<CoderChatMemory> chatMemory;

    private final ArmyVectorStore<CoderChatVectorStore> vectorStore;

    public MemoryTool(ArmyMessageChatMemory<CoderChatMemory> chatMemory, ArmyVectorStore<CoderChatVectorStore> vectorStore) {
        this.chatMemory = chatMemory;
        this.vectorStore = vectorStore;
    }


    @Tool(name = "ShortTermChatMemory", description = "short term chat memory")
    public List<Message> shortTermChatMemory(@ToolParam(description = "conversationId") String conversationId) {
        return this.chatMemory.get(conversationId);
    }


}
