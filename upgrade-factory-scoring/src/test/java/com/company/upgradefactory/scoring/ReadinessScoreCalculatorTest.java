package com.company.upgradefactory.scoring;

import com.company.upgradefactory.domain.model.RuleMatch;
import com.company.upgradefactory.scoring.service.ReadinessScoreCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadinessScoreCalculatorTest {

    @Test
    void shouldDeductPenaltyFromBaseScore() {
        ReadinessScoreCalculator calculator = new ReadinessScoreCalculator();
        int score = calculator.calculate(List.of(new RuleMatch("SEC-001", true, 12, "security legacy", List.of())));
        assertThat(score).isEqualTo(88);
    }
}
