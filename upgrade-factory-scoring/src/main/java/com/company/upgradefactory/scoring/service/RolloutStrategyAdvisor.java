package com.company.upgradefactory.scoring.service;

import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.enums.RolloutStrategy;

public class RolloutStrategyAdvisor {

    public RolloutStrategy advise(MigrationTier tier, boolean sharedQueueConsumer) {
        if (tier == MigrationTier.TIER_1_STRAIGHTFORWARD && !sharedQueueConsumer) {
            return RolloutStrategy.DIRECT_UPGRADE;
        }
        if (tier == MigrationTier.TIER_2_MODERATE) {
            return RolloutStrategy.ISOLATED_UAT_SOAK;
        }
        if (sharedQueueConsumer) {
            return RolloutStrategy.MANUAL_STAGED_CUTOVER;
        }
        return RolloutStrategy.SIDE_BY_SIDE_WITH_ISOLATION;
    }
}
