package com.company.upgradefactory.app.cli;

import com.company.upgradefactory.app.service.AssessmentApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AssessmentCliServiceTest {

    private final AssessmentCliService service = new AssessmentCliService(new AssessmentApplicationService());

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteJsonAndMarkdownReports() throws Exception {
        Path sampleRepo = Path.of("..", "upgrade-factory-testkit", "src", "test", "resources", "sample-repos", "sample-service-a")
                .toAbsolutePath()
                .normalize();

        int exitCode = service.execute(new String[]{
                "scan",
                "--repo", sampleRepo.toString(),
                "--repo-name", "sample-service-a",
                "--output-dir", tempDir.toString()
        });

        assertThat(exitCode).isEqualTo(0);
        assertThat(Files.readString(tempDir.resolve("upgrade-factory-report.json")))
                .contains("\"repoName\" : \"sample-service-a\"")
                .contains("\"readinessScore\" : 79");
        assertThat(Files.readString(tempDir.resolve("upgrade-factory-report.md")))
                .contains("# Upgrade Assessment")
                .contains("Repository: sample-service-a")
                .contains("Readiness Score: 79");
    }
}
