package com.company.upgradefactory.app.dto;

public record CommandExecutionResult(
        String stage,
        int exitCode,
        String standardOutput,
        String standardError,
        boolean successful) {
}
