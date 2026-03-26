package com.company.upgradefactory.domain.enums;

public enum RolloutStrategy {
    DIRECT_UPGRADE,
    ISOLATED_UAT_SOAK,
    SIDE_BY_SIDE_WITH_ISOLATION,
    MANUAL_STAGED_CUTOVER
}
