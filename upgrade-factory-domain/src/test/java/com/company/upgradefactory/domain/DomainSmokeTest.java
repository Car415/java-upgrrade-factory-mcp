package com.company.upgradefactory.domain;

import com.company.upgradefactory.domain.model.RepoDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainSmokeTest {

    @Test
    void shouldCreateRepoDescriptor() {
        RepoDescriptor descriptor = new RepoDescriptor("repo", "/tmp/repo", "main", "21", "3.5.12");
        assertThat(descriptor.repoName()).isEqualTo("repo");
    }
}
