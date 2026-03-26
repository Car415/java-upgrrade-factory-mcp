package com.company.upgradefactory.app;

import com.company.upgradefactory.app.dto.MigrationPlanRequest;
import com.company.upgradefactory.app.service.MigrationPlanApplicationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationPlanApplicationServiceTest {

    private final MigrationPlanApplicationService service = new MigrationPlanApplicationService();

    @Test
    void shouldGeneratePlanWithTierAndBlockerSpecificGuidance() {
        MigrationPlanRequest request = new MigrationPlanRequest(
                "sample-service-a",
                63,
                "TIER_2_MODERATE",
                "ISOLATED_UAT_SOAK",
                List.of(
                        "Refactor to jakarta.servlet and re-test servlet behavior.",
                        "Align maven compiler properties and toolchain to Java 21."
                )
        );

        var response = service.generatePlan(request);

        assertThat(response.executionPhases()).hasSize(3);
        assertThat(response.firstActions()).containsExactly(
                "Address blocker: Refactor to jakarta.servlet and re-test servlet behavior.",
                "Address blocker: Align maven compiler properties and toolchain to Java 21."
        );
        assertThat(response.validationFocusAreas())
                .contains("Review servlet, validation, and annotation namespace changes for Jakarta compatibility.")
                .contains("Verify compiler, plugin, and dependency-management changes in CI before rollout.");
        assertThat(response.summary()).contains("Readiness score is 63");
    }
}
