package com.company.upgradefactory.rules.loader;

import com.company.upgradefactory.rules.model.RuleDefinition;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleLoader {

    public RuleCatalog load(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return load(inputStream);
        }
    }

    @SuppressWarnings("unchecked")
    public RuleCatalog load(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> raw = yaml.load(inputStream);
        List<Map<String, Object>> rules = (List<Map<String, Object>>) raw.getOrDefault("rules", List.of());
        List<RuleDefinition> definitions = new ArrayList<>();
        for (Map<String, Object> rule : rules) {
            definitions.add(new RuleDefinition(
                    String.valueOf(rule.get("ruleId")),
                    String.valueOf(rule.get("category")),
                    String.valueOf(rule.get("description")),
                    String.valueOf(rule.get("severity")),
                    Integer.parseInt(String.valueOf(rule.get("penalty"))),
                    String.valueOf(rule.get("blockingLevel")),
                    String.valueOf(rule.get("recommendation"))
            ));
        }
        return new RuleCatalog(definitions);
    }
}
