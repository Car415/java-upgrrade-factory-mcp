package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.UpgradeExecutionPlan;
import com.company.upgradefactory.app.dto.UpgradeMode;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import com.company.upgradefactory.domain.model.RuleMatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeExecutionPlannerTest {

    private final UpgradeExecutionPlanner planner = new UpgradeExecutionPlanner(
            new OpenRewriteRecipeSelector(),
            new OpenRewriteCommandBuilder(new OpenRewriteRecipeArtifactCatalog())
    );

    @Test
    void shouldSelectRecipesAndAllowDryRunForModerateRepo() {
        AssessmentResult result = new AssessmentResult(
                new RepoDescriptor("sample-service-a", "D:/repo", "main", "21", "3.5.12"),
                63,
                53,
                MigrationTier.TIER_2_MODERATE,
                RolloutStrategy.ISOLATED_UAT_SOAK,
                List.of(
                        new RuleMatch("JAK-001", true, 8, "Refactor to jakarta.servlet and re-test servlet behavior.", List.of("javax.servlet")),
                        new RuleMatch("BUILD-001", true, 5, "Align maven compiler properties and toolchain to Java 21.", List.of("17"))
                ),
                "summary"
        );

        UpgradeExecutionPlan plan = planner.plan(result, UpgradeMode.DRY_RUN);

        assertThat(plan.mode()).isEqualTo(UpgradeMode.DRY_RUN);
        assertThat(plan.applyAllowed()).isTrue();
        assertThat(plan.selectedRecipes())
                .contains("org.openrewrite.java.migrate.UpgradeToJava21")
                .contains("org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta");
        assertThat(plan.executionCommands().getFirst())
                .contains("org.openrewrite.maven:rewrite-maven-plugin:dryRun")
                .contains("org.openrewrite.recipe:rewrite-migrate-java")
                .doesNotContain(" rewrite:dryRun");
        assertThat(plan.verificationCommands()).contains("mvn -q -DskipTests compile", "mvn -q test");
    }

    @Test
    void shouldAllowApplyForHardTierWhenUserExplicitlyRequestsIt() {
        AssessmentResult result = new AssessmentResult(
                new RepoDescriptor("hard-service", "D:/repo", "main", "21", "3.5.12"),
                41,
                21,
                MigrationTier.TIER_3_HARD,
                RolloutStrategy.MANUAL_STAGED_CUTOVER,
                List.of(new RuleMatch("SEC-001", true, 12, "Refactor to SecurityFilterChain-based configuration.", List.of("WebSecurityConfigurerAdapter"))),
                "summary"
        );

        UpgradeExecutionPlan plan = planner.plan(result, UpgradeMode.APPLY);

        assertThat(plan.applyAllowed()).isTrue();
        assertThat(plan.manualFollowUp()).contains("Review the generated dependency, configuration, and API changes before promotion.");
    }
}
