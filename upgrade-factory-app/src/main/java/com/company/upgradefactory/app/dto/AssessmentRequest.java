package com.company.upgradefactory.app.dto;

public record AssessmentRequest(
        String repoName,
        String repoPath,
        String branch,
        String targetJavaVersion,
        String targetSpringBootVersion) {
}
