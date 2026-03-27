package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.UpgradeExecutionPlan;
import com.company.upgradefactory.app.dto.UpgradeMode;
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
        boolean applyAllowed = true;
        List<String> manualFollowUp = new ArrayList<>();
        manualFollowUp.add("Review the generated dependency, configuration, and API changes before promotion.");
        if (result.blockers().stream().anyMatch(match -> match.penaltyApplied() >= 8)) {
            manualFollowUp.add("High-severity blockers remain. Treat the automated changes as a starting point and validate with compile, test, and UAT checks.");
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
