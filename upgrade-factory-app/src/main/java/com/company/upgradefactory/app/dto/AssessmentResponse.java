package com.company.upgradefactory.app.dto;

import java.util.List;

public record AssessmentResponse(
        String repoName,
        int readinessScore,
        int automationSuitability,
        String migrationTier,
        String rolloutStrategy,
        List<String> blockers,
        String summary) {
}
