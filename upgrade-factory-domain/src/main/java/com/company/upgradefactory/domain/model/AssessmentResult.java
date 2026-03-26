package com.company.upgradefactory.domain.model;

import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;

import java.util.List;

public record AssessmentResult(
        RepoDescriptor repoDescriptor,
        int readinessScore,
        int automationSuitability,
        MigrationTier migrationTier,
        RolloutStrategy rolloutStrategy,
        List<RuleMatch> blockers,
        String summary) {
}
