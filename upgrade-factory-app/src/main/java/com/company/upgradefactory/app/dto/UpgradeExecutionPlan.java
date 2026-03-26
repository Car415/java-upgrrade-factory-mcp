package com.company.upgradefactory.app.dto;

import java.util.List;

public record UpgradeExecutionPlan(
        UpgradeMode mode,
        boolean applyAllowed,
        java.util.List<String> selectedRecipes,
        java.util.List<String> executionCommands,
        java.util.List<String> manualFollowUp,
        java.util.List<String> verificationCommands) {
}
