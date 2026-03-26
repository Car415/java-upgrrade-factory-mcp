package com.company.upgradefactory.domain.model;

import com.company.upgradefactory.domain.enums.FindingCategory;
import com.company.upgradefactory.domain.enums.Severity;

import java.util.List;
import java.util.Map;

public record ScanFinding(
        String findingId,
        FindingCategory category,
        Severity severity,
        String message,
        List<String> evidence,
        List<String> filePaths,
        Map<String, Object> metadata) {
}
