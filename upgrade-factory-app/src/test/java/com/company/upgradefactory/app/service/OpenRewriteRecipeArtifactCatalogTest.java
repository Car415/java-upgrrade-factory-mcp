package com.company.upgradefactory.app.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRewriteRecipeArtifactCatalogTest {

    private final OpenRewriteRecipeArtifactCatalog catalog = new OpenRewriteRecipeArtifactCatalog();

    @Test
    void shouldResolveMinimalArtifactSetForSelectedRecipes() {
        List<String> artifacts = catalog.resolveArtifacts(List.of(
                "org.openrewrite.java.migrate.UpgradeToJava21",
                "org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta",
                "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5"
        ));

        assertThat(artifacts).containsExactly(
                "org.openrewrite.recipe:rewrite-migrate-java",
                "org.openrewrite.recipe:rewrite-spring"
        );
    }
}
