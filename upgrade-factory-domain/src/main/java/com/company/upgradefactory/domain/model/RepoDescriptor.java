package com.company.upgradefactory.domain.model;

public record RepoDescriptor(
        String repoName,
        String repoPath,
        String branch,
        String targetJavaVersion,
        String targetSpringBootVersion) {
}
