package com.company.upgradefactory.app.service;

import com.company.upgradefactory.ai.service.AssessmentNarrativeService;
import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.dto.AssessmentResponse;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import com.company.upgradefactory.domain.model.RuleMatch;
import com.company.upgradefactory.domain.model.ScanFinding;
import com.company.upgradefactory.rules.loader.RuleCatalog;
import com.company.upgradefactory.rules.loader.RuleLoader;
import com.company.upgradefactory.rules.model.RuleDefinition;
import com.company.upgradefactory.scanner.service.RepositoryScannerFacade;
import com.company.upgradefactory.scoring.service.ReadinessScoreCalculator;
import com.company.upgradefactory.scoring.service.RolloutStrategyAdvisor;
import com.company.upgradefactory.scoring.service.TierClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssessmentApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentApplicationService.class);
    private static final List<String> RULE_FILES = List.of(
            "build-rules.yml",
            "config-rules.yml",
            "framework-rules.yml",
            "jakarta-rules.yml",
            "messaging-rules.yml",
            "security-rules.yml",
            "test-rules.yml"
    );

    private final RepositoryScannerFacade repositoryScannerFacade = new RepositoryScannerFacade();
    private final ReadinessScoreCalculator readinessScoreCalculator = new ReadinessScoreCalculator();
    private final TierClassifier tierClassifier = new TierClassifier();
    private final RolloutStrategyAdvisor rolloutStrategyAdvisor = new RolloutStrategyAdvisor();
    private final AssessmentNarrativeService assessmentNarrativeService = new AssessmentNarrativeService();
    private final RuleLoader ruleLoader = new RuleLoader();

    public AssessmentResponse assess(AssessmentRequest request) throws IOException {
        return toResponse(assessResult(request));
    }

    public AssessmentResult assessResult(AssessmentRequest request) throws IOException {
        logger.info("Assessing repository '{}' at {}", request.repoName(), request.repoPath());
        RepoDescriptor descriptor = new RepoDescriptor(
                request.repoName(),
                request.repoPath(),
                request.branch() == null ? "main" : request.branch(),
                request.targetJavaVersion() == null ? "21" : request.targetJavaVersion(),
                request.targetSpringBootVersion() == null ? "3.5.12" : request.targetSpringBootVersion()
        );

        List<ScanFinding> findings = repositoryScannerFacade.scan(descriptor);
        Map<String, RuleDefinition> rulesById = loadRuleCatalog().rules().stream()
                .collect(Collectors.toMap(RuleDefinition::ruleId, Function.identity(), (left, right) -> left));
        List<RuleMatch> blockers = toRuleMatches(findings, rulesById);
        logger.info("Scan produced {} finding(s) and {} blocker(s) for '{}'", findings.size(), blockers.size(), request.repoName());

        int readinessScore = readinessScoreCalculator.calculate(blockers);
        int automationSuitability = calculateAutomationSuitability(readinessScore, blockers);
        MigrationTier tier = tierClassifier.classify(readinessScore);
        boolean sharedQueueConsumer = blockers.stream().anyMatch(match -> "MSG-003".equals(match.ruleId()));
        AssessmentResult baseResult = new AssessmentResult(
                descriptor,
                readinessScore,
                automationSuitability,
                tier,
                rolloutStrategyAdvisor.advise(tier, sharedQueueConsumer),
                blockers,
                "Evidence-driven assessment based on deterministic repository scanning and YAML rule evaluation."
        );
        logger.info("Assessment summary for '{}': readiness={}, automationSuitability={}, tier={}, rollout={}",
                request.repoName(), readinessScore, automationSuitability, tier, baseResult.rolloutStrategy());

        return new AssessmentResult(
                baseResult.repoDescriptor(),
                baseResult.readinessScore(),
                baseResult.automationSuitability(),
                baseResult.migrationTier(),
                baseResult.rolloutStrategy(),
                baseResult.blockers(),
                assessmentNarrativeService.summarize(baseResult)
        );
    }

    public AssessmentResponse toResponse(AssessmentResult result) {
        return new AssessmentResponse(
                result.repoDescriptor().repoName(),
                result.readinessScore(),
                result.automationSuitability(),
                result.migrationTier().name(),
                result.rolloutStrategy().name(),
                result.blockers().stream().map(RuleMatch::recommendation).toList(),
                result.summary()
        );
    }

    private RuleCatalog loadRuleCatalog() throws IOException {
        Path rulesRoot = locateRulesRoot();
        if (rulesRoot != null) {
            logger.debug("Loading rule catalog from filesystem path {}", rulesRoot);
            List<RuleDefinition> definitions = new ArrayList<>();
            try (Stream<Path> stream = Files.list(rulesRoot)) {
                for (Path ruleFile : stream
                        .filter(path -> path.getFileName().toString().endsWith(".yml"))
                        .sorted(Comparator.naturalOrder())
                        .toList()) {
                    definitions.addAll(ruleLoader.load(ruleFile).rules());
                }
            }
            return new RuleCatalog(definitions);
        }
        logger.debug("Loading rule catalog from classpath resources");
        return loadRuleCatalogFromClasspath();
    }

    private Path locateRulesRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve("config").resolve("rules");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }

    private RuleCatalog loadRuleCatalogFromClasspath() throws IOException {
        List<RuleDefinition> definitions = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        for (String ruleFile : RULE_FILES) {
            try (InputStream inputStream = classLoader.getResourceAsStream("rules/" + ruleFile)) {
                if (inputStream == null) {
                    throw new IOException("Required rule resource is missing from the packaged application: rules/" + ruleFile);
                }
                definitions.addAll(ruleLoader.load(inputStream).rules());
            }
        }
        return new RuleCatalog(definitions);
    }

    private List<RuleMatch> toRuleMatches(List<ScanFinding> findings, Map<String, RuleDefinition> rulesById) {
        return findings.stream()
                .collect(Collectors.toMap(
                        ScanFinding::findingId,
                        finding -> toRuleMatch(finding, rulesById.get(finding.findingId())),
                        this::pickHigherPenaltyMatch
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingInt(RuleMatch::penaltyApplied).reversed()
                        .thenComparing(RuleMatch::ruleId))
                .toList();
    }

    private RuleMatch toRuleMatch(ScanFinding finding, RuleDefinition ruleDefinition) {
        if (ruleDefinition == null) {
            return new RuleMatch(
                    finding.findingId(),
                    true,
                    defaultPenaltyFor(finding.category().name()),
                    finding.message(),
                    finding.evidence()
            );
        }
        return new RuleMatch(
                ruleDefinition.ruleId(),
                true,
                ruleDefinition.penalty(),
                ruleDefinition.recommendation(),
                finding.evidence().isEmpty() ? List.of(finding.message()) : finding.evidence()
        );
    }

    private RuleMatch pickHigherPenaltyMatch(RuleMatch left, RuleMatch right) {
        return left.penaltyApplied() >= right.penaltyApplied() ? left : right;
    }

    private int calculateAutomationSuitability(int readinessScore, List<RuleMatch> blockers) {
        long hardBlockers = blockers.stream().filter(match -> match.penaltyApplied() >= 8).count();
        return Math.max(readinessScore - (int) hardBlockers * 5, 0);
    }

    private int defaultPenaltyFor(String category) {
        return switch (category) {
            case "JAKARTA", "SECURITY" -> 10;
            case "BUILD", "FRAMEWORK", "TEST" -> 6;
            default -> 4;
        };
    }
}
