package com.company.upgradefactory.rules;

import com.company.upgradefactory.rules.loader.RuleLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RuleLoaderTest {

    @Test
    void shouldLoadBuildRules() throws Exception {
        RuleLoader loader = new RuleLoader();
        var catalog = loader.load(Path.of("../config/rules/build-rules.yml"));
        assertThat(catalog.rules()).isNotEmpty();
    }
}
