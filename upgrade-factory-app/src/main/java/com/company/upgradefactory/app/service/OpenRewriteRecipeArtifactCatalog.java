package com.company.upgradefactory.app.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OpenRewriteRecipeArtifactCatalog {

    public List<String> resolveArtifacts(List<String> recipes) {
        Set<String> artifacts = new LinkedHashSet<>();
        for (String recipe : recipes) {
            if (recipe.startsWith("org.openrewrite.java.migrate.")) {
                artifacts.add("org.openrewrite.recipe:rewrite-migrate-java");
            }
            if (recipe.startsWith("org.openrewrite.java.spring.")) {
                artifacts.add("org.openrewrite.recipe:rewrite-spring");
            }
        }
        return List.copyOf(artifacts);
    }
}
