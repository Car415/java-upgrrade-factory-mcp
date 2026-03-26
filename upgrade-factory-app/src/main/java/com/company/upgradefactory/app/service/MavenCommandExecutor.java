package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.CommandExecutionResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface MavenCommandExecutor {

    CommandExecutionResult execute(Path repoPath, List<String> command) throws IOException, InterruptedException;
}
