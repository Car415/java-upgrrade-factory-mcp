package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.CommandExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ProcessMavenCommandExecutor implements MavenCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ProcessMavenCommandExecutor.class);

    @Override
    public CommandExecutionResult execute(Path repoPath, List<String> command) throws IOException, InterruptedException {
        List<String> preparedCommand = prepareCommand(command);
        logger.info("Executing command in {}: {}", repoPath, String.join(" ", preparedCommand));
        Process process = new ProcessBuilder(preparedCommand)
                .directory(repoPath.toFile())
                .redirectErrorStream(false)
                .start();
        String standardOutput = new String(process.getInputStream().readAllBytes());
        String standardError = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();
        logger.info("Command completed with exit code {}", exitCode);
        if (!standardError.isBlank()) {
            logger.warn("Command stderr was not empty: {}", abbreviate(standardError));
        }
        return new CommandExecutionResult(
                command.isEmpty() ? "maven" : command.get(command.size() - 1),
                exitCode,
                standardOutput,
                standardError,
                exitCode == 0
        );
    }

    List<String> prepareCommand(List<String> command) {
        if (command == null || command.isEmpty()) {
            return List.of();
        }

        String executable = command.getFirst();
        if (isWindows() && "mvn".equals(executable)) {
            executable = "mvn.cmd";
        }

        if (executable.equals(command.getFirst())) {
            return command;
        }

        java.util.ArrayList<String> prepared = new java.util.ArrayList<>(command);
        prepared.set(0, executable);
        return List.copyOf(prepared);
    }

    private String abbreviate(String text) {
        String normalized = text.replace(System.lineSeparator(), " ").trim();
        if (normalized.length() <= 240) {
            return normalized;
        }
        return normalized.substring(0, 240) + "...";
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
