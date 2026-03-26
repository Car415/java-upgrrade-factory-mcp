package com.company.upgradefactory.app.service;

import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RuleMatch;

import java.util.LinkedHashSet;
import java.util.Set;

public class OpenRewriteRecipeSelector {

    public Set<String> selectRecipes(AssessmentResult result) {
        Set<String> recipes = new LinkedHashSet<>();
        recipes.add("org.openrewrite.java.migrate.UpgradeToJava21");
        recipes.add("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5");

        for (RuleMatch blocker : result.blockers()) {
            switch (blocker.ruleId()) {
                case "JAK-001", "JAK-004" -> recipes.add("org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta");
                case "SEC-001" -> recipes.add("org.openrewrite.java.spring.security6.UpgradeSpringSecurity_6");
                default -> {
                    // keep the initial version conservative and only add targeted recipes we understand.
                }
            }
        }
        return recipes;
    }
}
