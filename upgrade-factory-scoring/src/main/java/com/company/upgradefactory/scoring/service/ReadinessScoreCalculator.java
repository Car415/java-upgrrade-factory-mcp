package com.company.upgradefactory.scoring.service;

import com.company.upgradefactory.domain.model.RuleMatch;

import java.util.List;

public class ReadinessScoreCalculator {

    public int calculate(List<RuleMatch> ruleMatches) {
        int score = 100;
        for (RuleMatch match : ruleMatches) {
            if (match.matched()) {
                score -= match.penaltyApplied();
            }
        }
        return Math.max(score, 0);
    }
}
