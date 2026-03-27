package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.MigrationPlanRequest;
import com.company.upgradefactory.app.dto.MigrationPlanResponse;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MigrationPlanApplicationService {

    public MigrationPlanResponse generatePlan(MigrationPlanRequest request) {
        MigrationTier tier = MigrationTier.valueOf(request.migrationTier());
        RolloutStrategy rolloutStrategy = RolloutStrategy.valueOf(request.rolloutStrategy());
        List<String> blockers = request.blockers() == null ? List.of() : request.blockers();

        return new MigrationPlanResponse(
                request.repoName(),
                tier.name(),
                rolloutStrategy.name(),
                buildExecutionPhases(tier, rolloutStrategy),
                buildFirstActions(blockers),
                buildValidationFocusAreas(blockers, tier, rolloutStrategy),
                buildSummary(request.repoName(), request.readinessScore(), tier, rolloutStrategy, blockers)
        );
    }

    private List<String> buildExecutionPhases(MigrationTier tier, RolloutStrategy rolloutStrategy) {
        List<String> phases = new ArrayList<>();
        phases.add("Baseline the repository with a clean build, dependency inventory, and current test signal capture.");
        phases.add(switch (tier) {
            case TIER_1_STRAIGHTFORWARD ->
                    "Apply low-risk dependency and namespace upgrades, then run the full automated verification suite.";
            case TIER_2_MODERATE ->
                    "Resolve medium-risk framework and configuration gaps before applying the target runtime upgrade.";
            case TIER_3_HARD ->
                    "Break the migration into manual remediation tracks for framework, configuration, and runtime compatibility blockers.";
        });
        phases.add(switch (rolloutStrategy) {
            case DIRECT_UPGRADE ->
                    "Promote the upgraded build through standard environments with focused smoke and regression checks.";
            case ISOLATED_UAT_SOAK ->
                    "Run the upgraded service in an isolated UAT soak before production approval.";
            case SIDE_BY_SIDE_WITH_ISOLATION ->
                    "Validate old and new runtime behavior side by side with traffic or data isolation controls.";
            case MANUAL_STAGED_CUTOVER ->
                    "Execute a staged cutover plan with explicit rollback gates and operator checkpoints.";
        });
        return phases;
    }

    private List<String> buildFirstActions(List<String> blockers) {
        if (blockers.isEmpty()) {
            return List.of(
                    "Confirm the target Java and Spring Boot baselines in the build configuration.",
                    "Run compile, unit, and smoke checks before applying automated recipes.",
                    "Prepare a short rollback plan for the first promoted environment."
            );
        }

        return blockers.stream()
                .map(blocker -> "Address blocker: " + blocker)
                .limit(3)
                .toList();
    }

    private List<String> buildValidationFocusAreas(
            List<String> blockers,
            MigrationTier tier,
            RolloutStrategy rolloutStrategy
    ) {
        Set<String> focusAreas = new LinkedHashSet<>();
        if (tier != MigrationTier.TIER_1_STRAIGHTFORWARD) {
            focusAreas.add("Run deeper regression coverage for framework wiring, startup behavior, and external integrations.");
        }

        for (String blocker : blockers) {
            String normalized = blocker.toLowerCase(Locale.ROOT);
            if (normalized.contains("jakarta") || normalized.contains("javax")) {
                focusAreas.add("Review servlet, validation, and annotation namespace changes for Jakarta compatibility.");
            }
            if (normalized.contains("security")) {
                focusAreas.add("Validate authentication, authorization, and filter-chain behavior after the upgrade.");
            }
            if (normalized.contains("config")) {
                focusAreas.add("Compare environment-specific properties and bootstrap/application loading behavior.");
            }
            if (normalized.contains("messaging") || normalized.contains("queue") || normalized.contains("stream")) {
                focusAreas.add("Exercise messaging flows, retries, and error handling under realistic load.");
            }
            if (normalized.contains("build") || normalized.contains("java")) {
                focusAreas.add("Verify compiler, plugin, and dependency-management changes in CI before rollout.");
            }
        }

        if (rolloutStrategy == RolloutStrategy.MANUAL_STAGED_CUTOVER
                || rolloutStrategy == RolloutStrategy.SIDE_BY_SIDE_WITH_ISOLATION) {
            focusAreas.add("Define rollback triggers and production comparison signals before release approval.");
        }

        if (focusAreas.isEmpty()) {
            focusAreas.add("Validate startup, core user journeys, and production-adjacent configuration before promotion.");
        }

        return List.copyOf(focusAreas);
    }

    private String buildSummary(
            String repoName,
            int readinessScore,
            MigrationTier tier,
            RolloutStrategy rolloutStrategy,
            List<String> blockers
    ) {
        return "Migration plan for %s targets a %s execution path with %s rollout. Readiness score is %d and the plan is prioritized around %d known blockers."
                .formatted(repoName, tier.name(), rolloutStrategy.name(), readinessScore, blockers.size());
    }
}
