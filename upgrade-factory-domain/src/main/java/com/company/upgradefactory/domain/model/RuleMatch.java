package com.company.upgradefactory.domain.model;

import java.util.List;

public record RuleMatch(
        String ruleId,
        boolean matched,
        int penaltyApplied,
        String recommendation,
        List<String> evidence) {
}
