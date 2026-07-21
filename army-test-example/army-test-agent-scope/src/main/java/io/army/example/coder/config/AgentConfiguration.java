package io.army.example.coder.config;

import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class AgentConfiguration {

    @Bean
    public HarnessAgent harnessAgent() {
        return HarnessAgent.builder()
                .name("Army")
                .sysPrompt("你是一个帮助用户做笔记的助手。")
                .model("dashscope:qwen-plus")
                .workspace(Path.of(AgentEnv.armyProjectPath().toString(), ".trae"))
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build();
    }


}
