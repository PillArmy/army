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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /**
     * PostgreSQL 18.4 current version documentation search.
     */
    private static final String PG_DOC_SEARCH_URL = "https://www.postgresql.org/search/?q=";

    /**
     * Extract results from PostgreSQL search page.
     * Format: N. &lt;a href="URL"&gt;Title&lt;/a&gt; [score]&lt;br/&gt;&lt;div&gt;desc&lt;/div&gt;
     */
    private static final Pattern PG_RESULT_PATTERN = Pattern.compile(
            "\\d+\\.\\s*<a\\s+href=\"([^\"]+)\"[^>]*>([^<]+)</a>\\s*\\[([^\\]]+)\\]\\s*<br/>\\s*<div>(.*?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Tool(name = "pgDocSearch", description = "搜索 PostgreSQL 官方文档（当前版本 18.4）。输入函数名或关键词，返回官方文档中的相关条目、链接和描述。用于验证函数签名、返回类型、用法。")
    @SuppressWarnings("unused")
    public String pgDocSearch(String query) throws IOException, InterruptedException {
        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final String url = PG_DOC_SEARCH_URL + encodedQuery;

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Army-Agent/1.0")
                .GET()
                .build();

        final HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {
            return "PostgreSQL 文档搜索失败，HTTP 状态码: " + response.statusCode();
        }

        final String body = response.body();
        final Matcher matcher = PG_RESULT_PATTERN.matcher(body);

        final StringBuilder result = new StringBuilder();
        result.append("PostgreSQL 官方文档 (current) 搜索结果:\n\n");
        int count = 0;
        while (matcher.find() && count < 10) {
            final String href = matcher.group(1);          // URL
            final String title = matcher.group(2).trim();  // 文档标题
            final String score = matcher.group(3);          // 相关性评分
            String desc = matcher.group(4).replaceAll("<[^>]+>", "").trim(); // 摘要
            // 压缩空白
            desc = desc.replaceAll("\\s+", " ");
            if (desc.length() > 200) {
                desc = desc.substring(0, 200) + "...";
            }
            result.append("### ").append(title).append("\n");
            result.append("- URL: ").append(href).append("\n");
            result.append("- 相关性: ").append(score).append("\n");
            if (!desc.isEmpty()) {
                result.append("- 摘要: ").append(desc).append("\n");
            }
            result.append("\n");
            count++;
        }

        if (count == 0) {
            result.append("未找到匹配结果。请尝试更精确的关键词。\n");
            result.append("直接访问: https://www.postgresql.org/docs/current/");
        }

        return result.toString();
    }


}
