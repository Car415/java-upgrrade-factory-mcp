package com.company.upgradefactory.report.formatter;

import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RuleMatch;

public class MarkdownReportFormatter {

    public String format(AssessmentResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Upgrade Assessment\n\n");
        builder.append("- Repository: ").append(result.repoDescriptor().repoName()).append("\n");
        builder.append("- Readiness Score: ").append(result.readinessScore()).append("\n");
        builder.append("- Automation Suitability: ").append(result.automationSuitability()).append("\n");
        builder.append("- Migration Tier: ").append(result.migrationTier()).append("\n");
        builder.append("- Rollout Strategy: ").append(result.rolloutStrategy()).append("\n\n");
        builder.append("## Top Blockers\n");
        for (RuleMatch blocker : result.blockers()) {
            builder.append("- ").append(blocker.ruleId()).append(": ")
                    .append(blocker.recommendation()).append("\n");
        }
        builder.append("\n## Summary\n").append(result.summary()).append("\n");
        return builder.toString();
    }
}
