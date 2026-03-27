package com.company.upgradefactory.app.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessMavenCommandExecutorTest {

    private final ProcessMavenCommandExecutor executor = new ProcessMavenCommandExecutor();

    @Test
    void shouldResolveMavenExecutableForCurrentOperatingSystem() {
        List<String> resolved = executor.prepareCommand(List.of("mvn", "-q", "test"));

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            assertThat(resolved.getFirst()).isEqualTo("mvn.cmd");
        } else {
            assertThat(resolved.getFirst()).isEqualTo("mvn");
        }
    }
}
