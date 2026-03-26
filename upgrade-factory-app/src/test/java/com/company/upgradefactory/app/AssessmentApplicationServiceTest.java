package com.company.upgradefactory.app;

import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.service.AssessmentApplicationService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AssessmentApplicationServiceTest {

    private final AssessmentApplicationService service = new AssessmentApplicationService();

    @Test
    void shouldBuildAssessmentFromDeterministicFindingsAndRuleCatalog() throws IOException {
        Path sampleRepo = Path.of("..", "upgrade-factory-testkit", "src", "test", "resources", "sample-repos", "sample-service-a")
                .toAbsolutePath()
                .normalize();

        var response = service.assess(new AssessmentRequest(
                "sample-service-a",
                sampleRepo.toString(),
                "main",
                "21",
                "3.5.12"
        ));

        assertThat(response.repoName()).isEqualTo("sample-service-a");
        assertThat(response.readinessScore()).isEqualTo(79);
        assertThat(response.automationSuitability()).isEqualTo(69);
        assertThat(response.migrationTier()).isEqualTo("TIER_1_STRAIGHTFORWARD");
        assertThat(response.rolloutStrategy()).isEqualTo("DIRECT_UPGRADE");
        assertThat(response.blockers()).containsExactly(
                "Refactor to jakarta.servlet and re-test servlet behavior.",
                "Add baseline tests before attempting migration.",
                "Align maven compiler properties and toolchain to Java 21."
        );
        assertThat(response.summary())
                .contains("readiness score 79")
                .contains("recommended rollout is DIRECT_UPGRADE");
    }
}
