package io.army.example.coder.config;

import io.army.util._TimeUtils;
import org.springaicommunity.agent.tools.*;
import org.springaicommunity.agent.tools.task.TaskTool;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class AgentTool {


    static final OffsetDateTime BIRTH_DAY = OffsetDateTime.of(LocalDate.of(2019, 5, 10), LocalTime.of(2, 31, 0), ZoneOffset.ofHours(8));


    private AgentTool() {
    }

    static void createAgent(final ChatClient.Builder builder, Environment env) {

        builder.defaultTools(new AgentTool());


        builder.defaultTools(
                        createMainAgent(builder),
                        TodoWriteTool.builder().build(),

                        // common agentic tools
                        GlobTool.builder().build(),
                        GrepTool.builder().build(),
                        ShellTools.builder().build(),
                        FileSystemTools.builder().build(),

                        SmartWebFetchTool.builder(builder.clone().build()).build()
                        //   BraveWebSearchTool.builder(braveApiKey).resultCount(15).build()

                )
                .defaultSystem(p -> p.text(loadSystemPrompt()) // system prompt
                        .param(AgentEnv.ENVIRONMENT_INFO, AgentEnv.info(env))
                        .param(AgentEnv.GIT_STATUS, AgentEnv.gitStatus())
                        .param(AgentEnv.OFFSET_NOW, ArmyTemplateRenderer.OFFSET_NOW)
                        .param(AgentEnv.BIRTH_PERIOD, ArmyTemplateRenderer.BIRTH_PERIOD)
                )
        ;

    }


    private static ToolCallback createMainAgent(ChatClient.Builder builder) {
        final ClaudeSubagentType.Builder claudeBuilder;
        claudeBuilder = ClaudeSubagentType.builder();
        addSkillDir(claudeBuilder::skillsDirectories);
        claudeBuilder.chatClientBuilder("default", builder.clone());

        return TaskTool.builder()
                .subagentTypes(claudeBuilder.build())
                .build();
    }


    private static String loadSystemPrompt() {
        Path path = Path.of(AgentEnv.armyProjectPath().toString(), "army-test-example/army-test-coder/src/main/resources/prompt/Army.md");
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void addSkillDir(Consumer<String> consumer) {
        try (Stream<Path> stream = Files.walk(skillPaths(), 3)) {
            stream.filter(AgentTool::isSkill)
                    .map(Path::getParent)
                    .filter(AgentTool::isNotAgent)
                    .map(Path::toString)
                    .forEach(consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path skillPaths() {
        return Path.of(AgentEnv.armyProjectPath().toString(), ".trae", "skills");
    }

    private static boolean isNotAgent(Path path) {
        return !path.getFileName().toString().contains("agent");
    }


    private static boolean isSkill(Path path) {
        return Files.isRegularFile(path)
                && path.getFileName().toString().equals("Army.md");
    }


    @Tool(name = "nowTime", description = "Get the current time")
    @SuppressWarnings("unused")
    public String nowTime() {
        return OffsetDateTime.now().format(_TimeUtils.OFFSET_DATETIME_FORMATTER_6);
    }


}
