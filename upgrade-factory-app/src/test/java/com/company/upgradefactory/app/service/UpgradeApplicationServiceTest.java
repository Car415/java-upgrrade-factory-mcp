package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.dto.CommandExecutionResult;
import com.company.upgradefactory.app.dto.UpgradeMode;
import com.company.upgradefactory.app.dto.UpgradeReport;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeApplicationServiceTest {

    @Test
    void shouldReturnDryRunReportWithoutInvokingExecutor() throws Exception {
        FakeAssessmentApplicationService assessmentService = new FakeAssessmentApplicationService();
        RecordingMavenCommandExecutor executor = new RecordingMavenCommandExecutor();
        UpgradeApplicationService service = new UpgradeApplicationService(
                assessmentService,
                new UpgradeExecutionPlanner(
                        new OpenRewriteRecipeSelector(),
                        new OpenRewriteCommandBuilder(new OpenRewriteRecipeArtifactCatalog())
                ),
                executor
        );

        UpgradeReport report = service.executeUpgrade(Path.of("D:/repo"), "sample-service-a", "main", UpgradeMode.DRY_RUN);

        assertThat(report.mode()).isEqualTo(UpgradeMode.DRY_RUN);
        assertThat(report.commandResults()).isEmpty();
        assertThat(executor.executedCommands).isEmpty();
        assertThat(report.summary()).contains("dry-run");
    }

    @Test
    void shouldRunRewriteAndVerificationCommandsInApplyMode() throws Exception {
        FakeAssessmentApplicationService assessmentService = new FakeAssessmentApplicationService();
        RecordingMavenCommandExecutor executor = new RecordingMavenCommandExecutor();
        UpgradeApplicationService service = new UpgradeApplicationService(
                assessmentService,
                new UpgradeExecutionPlanner(
                        new OpenRewriteRecipeSelector(),
                        new OpenRewriteCommandBuilder(new OpenRewriteRecipeArtifactCatalog())
                ),
                executor
        );

        UpgradeReport report = service.executeUpgrade(Path.of("D:/repo"), "sample-service-a", "main", UpgradeMode.APPLY);

        assertThat(executor.executedCommands).hasSize(3);
        assertThat(executor.executedCommands.get(0))
                .contains("org.openrewrite.maven:rewrite-maven-plugin:run")
                .contains("-Drewrite.recipeArtifactCoordinates=");
        assertThat(report.commandResults()).hasSize(3);
        assertThat(report.postUpgradeAssessment()).isNotNull();
    }

    @Test
    void shouldSummarizeRewritePluginResolutionFailuresClearly() throws Exception {
        FakeAssessmentApplicationService assessmentService = new FakeAssessmentApplicationService();
        FailingRewriteMavenCommandExecutor executor = new FailingRewriteMavenCommandExecutor();
        UpgradeApplicationService service = new UpgradeApplicationService(
                assessmentService,
                new UpgradeExecutionPlanner(
                        new OpenRewriteRecipeSelector(),
                        new OpenRewriteCommandBuilder(new OpenRewriteRecipeArtifactCatalog())
                ),
                executor
        );

        UpgradeReport report = service.executeUpgrade(Path.of("D:/repo"), "sample-service-a", "main", UpgradeMode.APPLY);

        assertThat(report.summary()).contains("Rewrite plugin or recipe resolution failed");
    }

    private static final class FakeAssessmentApplicationService extends AssessmentApplicationService {

        @Override
        public AssessmentResult assessResult(AssessmentRequest request) {
            return new AssessmentResult(
                    new RepoDescriptor(request.repoName(), request.repoPath(), request.branch(), "21", "3.5.12"),
                    79,
                    69,
                    MigrationTier.TIER_1_STRAIGHTFORWARD,
                    RolloutStrategy.DIRECT_UPGRADE,
                    List.of(),
                    "summary"
            );
        }
    }

    private static final class RecordingMavenCommandExecutor implements MavenCommandExecutor {

        private final List<String> executedCommands = new ArrayList<>();

        @Override
        public CommandExecutionResult execute(Path repoPath, List<String> command) {
            executedCommands.add(String.join(" ", command));
            return new CommandExecutionResult(command.get(0), 0, "ok", "", true);
        }
    }

    private static final class FailingRewriteMavenCommandExecutor implements MavenCommandExecutor {

        private int invocationCount;

        @Override
        public CommandExecutionResult execute(Path repoPath, List<String> command) {
            invocationCount++;
            if (invocationCount == 1) {
                return new CommandExecutionResult(
                        "rewrite",
                        1,
                        "[ERROR] No plugin found for prefix 'rewrite'",
                        "",
                        false
                );
            }
            return new CommandExecutionResult("verification", 0, "ok", "", true);
        }
    }
}
