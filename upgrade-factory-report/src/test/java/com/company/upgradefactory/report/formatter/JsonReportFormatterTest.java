package com.company.upgradefactory.report.formatter;

import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import com.company.upgradefactory.domain.model.RuleMatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonReportFormatterTest {

    private final JsonReportFormatter formatter = new JsonReportFormatter();

    @Test
    void shouldFormatAssessmentResultAsPrettyJson() {
        AssessmentResult result = new AssessmentResult(
                new RepoDescriptor("sample-service-a", "D:/repo", "main", "21", "3.5.12"),
                79,
                69,
                MigrationTier.TIER_1_STRAIGHTFORWARD,
                RolloutStrategy.DIRECT_UPGRADE,
                List.of(new RuleMatch("JAK-001", true, 8, "Refactor to jakarta.servlet", List.of("javax.servlet"))),
                "Sample summary"
        );

        String json = formatter.format(result);

        assertThat(json).contains("\"repoName\" : \"sample-service-a\"");
        assertThat(json).contains("\"readinessScore\" : 79");
        assertThat(json).contains("\"summary\" : \"Sample summary\"");
    }
}
