package com.company.upgradefactory.app.cli;

import com.company.upgradefactory.app.dto.UpgradeMode;
import com.company.upgradefactory.app.dto.UpgradeReport;
import com.company.upgradefactory.app.service.UpgradeApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class UpgradeCliService {

    private static final String JSON_REPORT_NAME = "upgrade-factory-upgrade-report.json";
    private static final String MARKDOWN_REPORT_NAME = "upgrade-factory-upgrade-report.md";

    private final UpgradeApplicationService upgradeApplicationService;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public UpgradeCliService(UpgradeApplicationService upgradeApplicationService) {
        this.upgradeApplicationService = upgradeApplicationService;
    }

    public int execute(String[] args) throws Exception {
        Map<String, String> options = parseOptions(args);
        Path repoPath = resolveRepoPath(options.get("repo"));
        Path outputDirectory = resolveOutputDirectory(options.get("output-dir"), repoPath);
        String repoName = options.getOrDefault("repo-name", repoPath.getFileName().toString());
        String branch = options.getOrDefault("branch", "main");
        UpgradeMode mode = "true".equalsIgnoreCase(options.getOrDefault("apply", "false"))
                ? UpgradeMode.APPLY
                : UpgradeMode.DRY_RUN;

        UpgradeReport report = upgradeApplicationService.executeUpgrade(repoPath, repoName, branch, mode);
        Files.createDirectories(outputDirectory);
        Path jsonReport = outputDirectory.resolve(JSON_REPORT_NAME);
        Path markdownReport = outputDirectory.resolve(MARKDOWN_REPORT_NAME);
        Files.writeString(jsonReport, objectMapper.writeValueAsString(report));
        Files.writeString(markdownReport, formatMarkdown(report));
        return 0;
    }

    private Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int index = 1; index < args.length; index++) {
            String token = args[index];
            if (!token.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + token);
            }
            String key = token.substring(2);
            if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                throw new IllegalArgumentException("Missing value for option --" + key);
            }
            options.put(key, args[++index]);
        }
        return options;
    }

    private Path resolveRepoPath(String repoArgument) {
        Path repoPath = repoArgument == null
                ? Path.of("").toAbsolutePath().normalize()
                : Path.of(repoArgument).toAbsolutePath().normalize();
        if (!Files.exists(repoPath.resolve("pom.xml"))) {
            throw new IllegalArgumentException("Repository path must point to a Maven project root containing pom.xml: " + repoPath);
        }
        return repoPath;
    }

    private Path resolveOutputDirectory(String outputArgument, Path repoPath) {
        return outputArgument == null
                ? repoPath
                : Path.of(outputArgument).toAbsolutePath().normalize();
    }

    private String formatMarkdown(UpgradeReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Upgrade Transformation Report\n\n");
        builder.append("- Repository: ").append(report.repoName()).append("\n");
        builder.append("- Execution Mode: ").append(report.mode()).append("\n");
        builder.append("- Pre-Upgrade Score: ").append(report.preUpgradeAssessment().readinessScore()).append("\n\n");
        builder.append("## Selected Recipes\n");
        for (String recipe : report.executionPlan().selectedRecipes()) {
            builder.append("- ").append(recipe).append("\n");
        }
        builder.append("\n## Summary\n").append(report.summary()).append("\n");
        return builder.toString();
    }
}
