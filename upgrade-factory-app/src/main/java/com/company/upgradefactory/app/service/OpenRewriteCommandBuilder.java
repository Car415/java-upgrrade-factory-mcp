package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.UpgradeMode;

import java.util.List;

public class OpenRewriteCommandBuilder {

    private final OpenRewriteRecipeArtifactCatalog recipeArtifactCatalog;

    public OpenRewriteCommandBuilder(OpenRewriteRecipeArtifactCatalog recipeArtifactCatalog) {
        this.recipeArtifactCatalog = recipeArtifactCatalog;
    }

    public String buildCommand(UpgradeMode mode, List<String> selectedRecipes) {
        String goal = mode == UpgradeMode.APPLY
                ? "org.openrewrite.maven:rewrite-maven-plugin:run"
                : "org.openrewrite.maven:rewrite-maven-plugin:dryRun";
        List<String> recipeArtifacts = recipeArtifactCatalog.resolveArtifacts(selectedRecipes);

        StringBuilder command = new StringBuilder("mvn -U ");
        command.append(goal);
        command.append(" -Drewrite.activeRecipes=").append(String.join(",", selectedRecipes));
        if (!recipeArtifacts.isEmpty()) {
            command.append(" -Drewrite.recipeArtifactCoordinates=").append(String.join(",", recipeArtifacts));
        }
        return command.toString();
    }
}
