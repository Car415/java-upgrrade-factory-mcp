package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.UpgradeExecutionPlan;
import com.company.upgradefactory.app.dto.UpgradeMode;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.model.AssessmentResult;

import java.util.ArrayList;
import java.util.List;

public class UpgradeExecutionPlanner {

    private final OpenRewriteRecipeSelector recipeSelector;

    public UpgradeExecutionPlanner(OpenRewriteRecipeSelector recipeSelector) {
        this.recipeSelector = recipeSelector;
    }

    public UpgradeExecutionPlan plan(AssessmentResult result, UpgradeMode mode) {
        List<String> selectedRecipes = new ArrayList<>(recipeSelector.selectRecipes(result));
        boolean applyAllowed = result.migrationTier() != MigrationTier.TIER_3_HARD;
        List<String> manualFollowUp = new ArrayList<>();
        manualFollowUp.add("Review the generated dependency, configuration, and API changes before promotion.");
        if (!applyAllowed) {
            manualFollowUp.add("Apply mode is blocked for Tier 3 repositories in the initial CLI implementation.");
        }

        List<String> executionCommands = List.of(buildRewriteCommand(mode, selectedRecipes));
        List<String> verificationCommands = List.of(
                "mvn -q -DskipTests compile",
                "mvn -q test"
        );
        return new UpgradeExecutionPlan(
                mode,
                applyAllowed,
                selectedRecipes,
                executionCommands,
                manualFollowUp,
                verificationCommands
        );
    }

    private String buildRewriteCommand(UpgradeMode mode, List<String> selectedRecipes) {
        String goal = mode == UpgradeMode.APPLY ? "rewrite:run" : "rewrite:dryRun";
        return "mvn -q -Drewrite.activeRecipes=%s %s"
                .formatted(String.join(",", selectedRecipes), goal);
    }
}
