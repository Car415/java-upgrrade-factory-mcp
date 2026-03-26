package com.company.upgradefactory.ai.service;

import com.company.upgradefactory.domain.model.AssessmentResult;

public class AssessmentNarrativeService {

    public String summarize(AssessmentResult result) {
        String topRecommendation = result.blockers().stream()
                .findFirst()
                .map(blocker -> " Top recommendation: " + blocker.recommendation())
                .orElse("");
        return "Repository %s is classified as %s with readiness score %d. Automation suitability is %d and the recommended rollout is %s. Primary blockers count: %d.%s"
                .formatted(
                        result.repoDescriptor().repoName(),
                        result.migrationTier(),
                        result.readinessScore(),
                        result.automationSuitability(),
                        result.rolloutStrategy(),
                        result.blockers().size(),
                        topRecommendation);
    }
}
