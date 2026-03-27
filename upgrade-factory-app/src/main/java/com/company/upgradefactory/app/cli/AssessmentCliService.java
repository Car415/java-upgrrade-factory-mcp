package com.company.upgradefactory.app.cli;

import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.service.AssessmentApplicationService;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.report.formatter.JsonReportFormatter;
import com.company.upgradefactory.report.formatter.MarkdownReportFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class AssessmentCliService {

    private static final String JSON_REPORT_NAME = "upgrade-factory-report.json";
    private static final String MARKDOWN_REPORT_NAME = "upgrade-factory-report.md";
    private static final Logger logger = LoggerFactory.getLogger(AssessmentCliService.class);

    private final AssessmentApplicationService assessmentApplicationService;
    private final JsonReportFormatter jsonReportFormatter = new JsonReportFormatter();
    private final MarkdownReportFormatter markdownReportFormatter = new MarkdownReportFormatter();

    public AssessmentCliService(AssessmentApplicationService assessmentApplicationService) {
        this.assessmentApplicationService = assessmentApplicationService;
    }

    public int execute(String[] args) throws IOException {
        if (args.length == 0 || isHelp(args[0])) {
            printUsage();
            return 0;
        }

        if (!"scan".equals(args[0])) {
            System.err.println("Unsupported command: " + args[0]);
            printUsage();
            return 1;
        }

        Map<String, String> options = parseOptions(args);
        Path repoPath = resolveRepoPath(options.get("repo"));
        Path outputDirectory = resolveOutputDirectory(options.get("output-dir"), repoPath);
        String repoName = options.getOrDefault("repo-name", repoPath.getFileName().toString());
        logger.info("Running scan for repository '{}' at {}", repoName, repoPath);

        AssessmentResult result = assessmentApplicationService.assessResult(new AssessmentRequest(
                repoName,
                repoPath.toString(),
                options.get("branch"),
                options.get("target-java-version"),
                options.get("target-spring-boot-version")
        ));

        Files.createDirectories(outputDirectory);
        Path jsonReport = outputDirectory.resolve(JSON_REPORT_NAME);
        Path markdownReport = outputDirectory.resolve(MARKDOWN_REPORT_NAME);
        Files.writeString(jsonReport, jsonReportFormatter.format(result));
        Files.writeString(markdownReport, markdownReportFormatter.format(result));
        logger.info("Assessment complete for '{}' with readiness score {} and {} blocker(s)",
                repoName, result.readinessScore(), result.blockers().size());
        logger.info("Reports written to {} and {}", jsonReport, markdownReport);

        printSummary(result, jsonReport, markdownReport);
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
        logger.debug("Parsed scan options: {}", options.keySet());
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

    private boolean isHelp(String arg) {
        return "help".equals(arg) || "--help".equals(arg) || "-h".equals(arg);
    }

    private void printSummary(AssessmentResult result, Path jsonReport, Path markdownReport) {
        System.out.println("Upgrade Factory scan completed.");
        System.out.println("Repository: " + result.repoDescriptor().repoName());
        System.out.println("Readiness Score: " + result.readinessScore());
        System.out.println("Migration Tier: " + result.migrationTier());
        System.out.println("Rollout Strategy: " + result.rolloutStrategy());
        System.out.println("JSON Report: " + jsonReport);
        System.out.println("Markdown Report: " + markdownReport);
    }

    private void printUsage() {
        System.out.println("Usage: java -jar upgrade-factory-app.jar scan [options]");
        System.out.println("  --repo <path>                        Repository root to scan. Defaults to the current directory.");
        System.out.println("  --repo-name <name>                   Override the repository display name.");
        System.out.println("  --branch <name>                      Override the branch name recorded in the report.");
        System.out.println("  --target-java-version <version>      Target Java version. Defaults to 21.");
        System.out.println("  --target-spring-boot-version <ver>   Target Spring Boot version. Defaults to 3.5.12.");
        System.out.println("  --output-dir <path>                  Directory where JSON and Markdown reports are written.");
    }
}
