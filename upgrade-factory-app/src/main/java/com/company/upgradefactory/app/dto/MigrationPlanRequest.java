package com.company.upgradefactory.app.dto;

import java.util.List;

public record MigrationPlanRequest(
        String repoName,
        int readinessScore,
        String migrationTier,
        String rolloutStrategy,
        List<String> blockers) {
}
