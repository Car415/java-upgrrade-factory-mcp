package com.company.upgradefactory.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record MigrationPlanRequest(
        @NotBlank String repoName,
        @Min(0) @Max(100) int readinessScore,
        @NotBlank String migrationTier,
        @NotBlank String rolloutStrategy,
        List<String> blockers) {
}
