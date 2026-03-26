package com.company.upgradefactory.app.cli;

import com.company.upgradefactory.app.dto.CommandExecutionResult;
import com.company.upgradefactory.app.dto.UpgradeExecutionPlan;
import com.company.upgradefactory.app.dto.UpgradeMode;
import com.company.upgradefactory.app.dto.UpgradeReport;
import com.company.upgradefactory.app.service.UpgradeApplicationService;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeCliServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDefaultUpgradeCommandToDryRunAndWriteReports() throws Exception {
        Path sampleRepo = Path.of("..", "upgrade-factory-testkit", "src", "test", "resources", "sample-repos", "sample-service-a")
                .toAbsolutePath()
                .normalize();
        CapturingUpgradeApplicationService applicationService = new CapturingUpgradeApplicationService();
        UpgradeCliService service = new UpgradeCliService(applicationService);

        int exitCode = service.execute(new String[]{
                "upgrade",
                "--repo", sampleRepo.toString(),
                "--output-dir", tempDir.toString()
        });

        assertThat(exitCode).isEqualTo(0);
        assertThat(applicationService.lastMode).isEqualTo(UpgradeMode.DRY_RUN);
        assertThat(Files.readString(tempDir.resolve("upgrade-factory-upgrade-report.json")))
                .contains("\"mode\" : \"DRY_RUN\"");
        assertThat(Files.readString(tempDir.resolve("upgrade-factory-upgrade-report.md")))
                .contains("# Upgrade Transformation Report")
                .contains("Execution Mode: DRY_RUN");
    }

    @Test
    void shouldUseApplyModeWhenFlagIsProvided() throws Exception {
        Path sampleRepo = Path.of("..", "upgrade-factory-testkit", "src", "test", "resources", "sample-repos", "sample-service-a")
                .toAbsolutePath()
                .normalize();
        CapturingUpgradeApplicationService applicationService = new CapturingUpgradeApplicationService();
        UpgradeCliService service = new UpgradeCliService(applicationService);

        int exitCode = service.execute(new String[]{
                "upgrade",
                "--repo", sampleRepo.toString(),
                "--apply", "true",
                "--output-dir", tempDir.toString()
        });

        assertThat(exitCode).isEqualTo(0);
        assertThat(applicationService.lastMode).isEqualTo(UpgradeMode.APPLY);
    }

    private static final class CapturingUpgradeApplicationService extends UpgradeApplicationService {

        private UpgradeMode lastMode;

        @Override
        public UpgradeReport executeUpgrade(Path repoPath, String repoName, String branch, UpgradeMode mode) {
            this.lastMode = mode;
            return new UpgradeReport(
                    repoName,
                    repoPath.toString(),
                    mode,
                    new AssessmentResult(
                            new RepoDescriptor(repoName, repoPath.toString(), branch, "21", "3.5.12"),
                            79,
                            69,
                            MigrationTier.TIER_1_STRAIGHTFORWARD,
                            RolloutStrategy.DIRECT_UPGRADE,
                            List.of(),
                            "before"
                    ),
                    new UpgradeExecutionPlan(
                            mode,
                            true,
                            List.of("org.openrewrite.java.migrate.UpgradeToJava21"),
                            List.of("mvn rewrite:dryRun"),
                            List.of("Review jakarta migration"),
                            List.of("Run mvn test")
                    ),
                    List.of(new CommandExecutionResult("rewrite", 0, "ok", "", true)),
                    null,
                    List.of("Review jakarta migration"),
                    List.of("Run mvn test"),
                    "dry run summary"
            );
        }
    }
}
