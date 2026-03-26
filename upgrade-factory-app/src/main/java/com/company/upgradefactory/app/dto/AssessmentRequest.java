package com.company.upgradefactory.app.dto;

import jakarta.validation.constraints.NotBlank;

public record AssessmentRequest(
        @NotBlank String repoName,
        @NotBlank String repoPath,
        String branch,
        String targetJavaVersion,
        String targetSpringBootVersion) {
}
