package io.army.example.coder.config;

import io.army.dialect._Constant;
import io.army.util._StringUtils;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

final class AgentEnv {


    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    static final String ENVIRONMENT_INFO = "ENVIRONMENT_INFO";

    static final String GIT_STATUS = "GIT_STATUS";

    static final String OFFSET_NOW = "OFFSET_NOW";

    static final String BIRTH_PERIOD = "BIRTH_PERIOD";


    static Path armyProjectPath() {
        Path path = Path.of(System.getProperty("user.dir"));

        for (String fileName = path.getFileName().toString(); !fileName.equals("army"); fileName = path.getFileName().toString()) {
            path = path.getParent();
        }
        return path.toAbsolutePath();
    }


    static String info(Environment env) {

        final boolean isGitRepo;
        isGitRepo = Files.exists(Path.of(armyProjectPath().toString(), ".git"));

        final Path armyPath = armyProjectPath();

        return _StringUtils.builder(512)
                .append("Working directory: ")
                .append(System.getProperty("user.dir"))
                .append(_Constant.LF)

                .append("Is directory a git repo: ")
                .append(isGitRepo ? "Yes" : "No")
                .append(_Constant.LF)

                .append("Platform: ")
                .append(System.getProperty("os.name").toLowerCase())
                .append(_Constant.LF)

                .append("OS Version: ")
                .append(System.getProperty("os.name"))
                .append(_Constant.SPACE)
                .append(System.getProperty("os.version"))
                .append(_Constant.LF)

                .append("The programming language :  Java")
                .append(_Constant.LF)

                .append("The framework :  Spring Boot")
                .append(_Constant.LF)

                .append("Java version : ")
                .append(System.getProperty("java.version"))
                .append(_Constant.LF)

                .append("Army project directory : ")
                .append(armyPath)
                .append(_Constant.LF)

                .append("Your skills directory : ")
                .append(Path.of(armyPath.toString(), ".trae/skills"))
                .append(_Constant.LF)

                .append("Your system prompt directory : ")
                .append(Path.of(armyPath.toString(), "army-test-example/army-test-coder/src/main/resources/prompt"))
                .append(_Constant.LF)

                .append("Your memory directory : ")
                .append(Path.of(armyPath.toString(), ".trae/memory"))
                .append(_Constant.LF)

                .append("Your rules directory : ")
                .append(Path.of(armyPath.toString(), ".trae/rules"))
                .append(_Constant.LF)

                .append("Your geographical location : ")
                .append(env.getRequiredProperty("user.region"))
                .append(_Constant.LF)

                .append("Your output client : Chinese markdown")

                .toString();

    }

    static String gitStatus() {

        // Check if git is available
        if (!isGitAvailable()) {
            System.out.println("Git is not available or not in PATH.\n");
            return "";
        }

        // Check if we're in a git repository
        String gitCheck = runGitCommand("rev-parse", "--is-inside-work-tree");
        if (!"true".equals(gitCheck)) {
            System.out.println("Not inside a git repository.\n");
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("gitStatus: This is the git status at the start of the conversation. ");
        sb.append("Note that this status is a snapshot in time, and will not update during the conversation.\n");

        // Get current branch
        String currentBranch = runGitCommand("rev-parse", "--abbrev-ref", "HEAD");
        sb.append("Current branch: ").append(currentBranch).append("\n\n");

        // Get main/master branch (for PRs)
        String mainBranch = getMainBranch();
        sb.append("Main branch (you will usually use this for PRs): ").append(mainBranch).append("\n\n");

        // Get git status
        String status = runGitCommand("status", "--short");
        sb.append("Status:\n").append(status.isEmpty() ? "Working tree clean\n\n" : status).append("\n\n");

        // Get recent commits
        String recentCommits = runGitCommand("log", "--oneline", "-n", "5");
        sb.append("Recent commits:\n").append(recentCommits);

        return sb.toString();
    }

    private static boolean isGitAvailable() {
        try {
            String result = runGitCommand("--version");
            return result != null && result.contains("git version");
        } catch (Exception e) {
            return false;
        }
    }

    private static String getMainBranch() {
        // Try to detect the main branch name
        String[] possibleMains = {"main", "master"};
        for (String branch : possibleMains) {
            String result = runGitCommand("rev-parse", "--verify", "--quiet", branch);
            if (result != null && !result.isEmpty() && !result.toLowerCase().contains("fatal")) {
                return branch;
            }
        }
        // Try to get from remote
        String remoteBranch = runGitCommand("symbolic-ref", "refs/remotes/origin/HEAD", "--short");
        if (remoteBranch != null && !remoteBranch.isEmpty()) {
            return remoteBranch.replace("origin/", "");
        }
        return "main";
    }

    /**
     * Runs a git command in a cross-platform manner. On Windows, uses cmd.exe /c to
     * ensure proper command execution. On Unix/Mac, runs git directly.
     */
    private static String runGitCommand(String... gitArgs) {
        try {
            List<String> command = new ArrayList<>();

            if (IS_WINDOWS) {
                command.add("cmd.exe");
                command.add("/c");
                command.add("git");
            } else {
                command.add("git");
            }

            for (String arg : gitArgs) {
                command.add(arg);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);

            // Set environment to ensure consistent output
            pb.environment().put("LC_ALL", "C");
            pb.environment().put("LANG", "C");

            Process process = pb.start();

            String result;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                result = reader.lines().collect(Collectors.joining("\n"));
            }

            // Wait with timeout to prevent hanging
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "";
            }

            return result.trim();
        } catch (Exception e) {
            return "";
        }
    }

}
