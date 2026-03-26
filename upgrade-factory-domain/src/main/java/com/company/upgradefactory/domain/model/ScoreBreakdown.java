package com.company.upgradefactory.domain.model;

import java.util.Map;

public record ScoreBreakdown(
        int baseScore,
        int finalScore,
        Map<String, Integer> penaltiesByCategory) {
}
