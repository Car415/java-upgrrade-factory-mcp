package com.company.upgradefactory.rules.model;

public record RuleDefinition(
        String ruleId,
        String category,
        String description,
        String severity,
        int penalty,
        String blockingLevel,
        String recommendation) {
}
