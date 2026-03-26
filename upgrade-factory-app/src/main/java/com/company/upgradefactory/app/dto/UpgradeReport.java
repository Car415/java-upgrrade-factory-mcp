package com.company.upgradefactory.app.dto;

import com.company.upgradefactory.domain.model.AssessmentResult;

import java.util.List;

public record UpgradeReport(
        String repoName,
        String repoPath,
        UpgradeMode mode,
        AssessmentResult preUpgradeAssessment,
        UpgradeExecutionPlan executionPlan,
        List<CommandExecutionResult> commandResults,
        AssessmentResult postUpgradeAssessment,
        List<String> remainingBlockers,
        List<String> validationSteps,
        String summary) {
}
