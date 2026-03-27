package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.UpgradeMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRewriteCommandBuilderTest {

    private final OpenRewriteCommandBuilder builder = new OpenRewriteCommandBuilder(new OpenRewriteRecipeArtifactCatalog());

    @Test
    void shouldBuildFullyQualifiedRewriteRunCommandWithRecipeArtifacts() {
        String command = builder.buildCommand(
                UpgradeMode.APPLY,
                List.of(
                        "org.openrewrite.java.migrate.UpgradeToJava21",
                        "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5",
                        "org.openrewrite.java.spring.security6.UpgradeSpringSecurity_6"
                )
        );

        assertThat(command)
                .contains("mvn -U")
                .contains("org.openrewrite.maven:rewrite-maven-plugin:run")
                .contains("-Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21,org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5,org.openrewrite.java.spring.security6.UpgradeSpringSecurity_6")
                .contains("-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java,org.openrewrite.recipe:rewrite-spring");
    }

    @Test
    void shouldBuildFullyQualifiedRewriteDryRunCommand() {
        String command = builder.buildCommand(
                UpgradeMode.DRY_RUN,
                List.of("org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta")
        );

        assertThat(command)
                .contains("org.openrewrite.maven:rewrite-maven-plugin:dryRun")
                .contains("org.openrewrite.recipe:rewrite-migrate-java");
    }
}
