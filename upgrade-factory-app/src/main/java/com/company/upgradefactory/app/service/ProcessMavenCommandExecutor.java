package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.CommandExecutionResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class ProcessMavenCommandExecutor implements MavenCommandExecutor {

    @Override
    public CommandExecutionResult execute(Path repoPath, List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .directory(repoPath.toFile())
                .redirectErrorStream(false)
                .start();
        String standardOutput = new String(process.getInputStream().readAllBytes());
        String standardError = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();
        return new CommandExecutionResult(
                command.isEmpty() ? "maven" : command.get(command.size() - 1),
                exitCode,
                standardOutput,
                standardError,
                exitCode == 0
        );
    }
}
