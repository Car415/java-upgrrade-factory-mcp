package com.company.upgradefactory.app.dto;

import java.util.List;

public record MigrationPlanResponse(
        String repoName,
        String migrationTier,
        String rolloutStrategy,
        List<String> executionPhases,
        List<String> firstActions,
        List<String> validationFocusAreas,
        String summary) {
}
