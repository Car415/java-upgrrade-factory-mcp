package com.company.upgradefactory.scoring.service;

import com.company.upgradefactory.domain.enums.MigrationTier;

public class TierClassifier {

    public MigrationTier classify(int readinessScore) {
        if (readinessScore >= 75) {
            return MigrationTier.TIER_1_STRAIGHTFORWARD;
        }
        if (readinessScore >= 50) {
            return MigrationTier.TIER_2_MODERATE;
        }
        return MigrationTier.TIER_3_HARD;
    }
}
