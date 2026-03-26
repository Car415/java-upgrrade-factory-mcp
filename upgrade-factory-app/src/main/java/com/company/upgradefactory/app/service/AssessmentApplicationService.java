package com.company.upgradefactory.app.service;

import com.company.upgradefactory.ai.service.AssessmentNarrativeService;
import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.dto.AssessmentResponse;
import com.company.upgradefactory.domain.enums.MigrationTier;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import com.company.upgradefactory.domain.model.RuleMatch;
import com.company.upgradefactory.rules.loader.RuleCatalog;
import com.company.upgradefactory.rules.loader.RuleLoader;
import com.company.upgradefactory.rules.model.RuleDefinition;
import com.company.upgradefactory.scanner.service.RepositoryScannerFacade;
import com.company.upgradefactory.scoring.service.ReadinessScoreCalculator;
import com.company.upgradefactory.scoring.service.RolloutStrategyAdvisor;
import com.company.upgradefactory.scoring.service.TierClassifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AssessmentApplicationService {

    private final RepositoryScannerFacade repositoryScannerFacade = new RepositoryScannerFacade();
    private final ReadinessScoreCalculator readinessScoreCalculator = new ReadinessScoreCalculator();
    private final TierClassifier tierClassifier = new TierClassifier();
    private final RolloutStrategyAdvisor rolloutStrategyAdvisor = new RolloutStrategyAdvisor();
    private final AssessmentNarrativeService assessmentNarrativeService = new AssessmentNarrativeService();
    private final RuleLoader ruleLoader = new RuleLoader();

    public AssessmentResponse assess(AssessmentRequest request) throws IOException {
        RepoDescriptor descriptor = new RepoDescriptor(
                request.repoName(),
                request.repoPath(),
                request.branch() == null ? "main" : request.branch(),
                request.targetJavaVersion() == null ? "21" : request.targetJavaVersion(),
                request.targetSpringBootVersion() == null ? "3.5.12" : request.targetSpringBootVersion()
        );

        var findings = repositoryScannerFacade.scan(descriptor);
        Map<String, RuleDefinition> rulesById = loadRuleCatalog().rules().stream()
                .collect(Collectors.toMap(RuleDefinition::ruleId, Function.identity(), (left, right) -> left));
        List<RuleMatch> blockers = toRuleMatches(findings, rulesById);

        int readinessScore = readinessScoreCalculator.calculate(blockers);
        int automationSuitability = calculateAutomationSuitability(readinessScore, blockers);
        MigrationTier tier = tierClassifier.classify(readinessScore);
        boolean sharedQueueConsumer = blockers.stream().anyMatch(match -> "MSG-003".equals(match.ruleId()));
        AssessmentResult result = new AssessmentResult(
                descriptor,
                readinessScore,
                automationSuitability,
                tier,
                rolloutStrategyAdvisor.advise(tier, sharedQueueConsumer),
                blockers,
                "Evidence-driven assessment based on deterministic repository scanning and YAML rule evaluation."
        );

        return new AssessmentResponse(
                result.repoDescriptor().repoName(),
                result.readinessScore(),
                result.automationSuitability(),
                result.migrationTier().name(),
                result.rolloutStrategy().name(),
                result.blockers().stream().map(RuleMatch::recommendation).toList(),
                assessmentNarrativeService.summarize(result)
        );
    }

    private RuleCatalog loadRuleCatalog() throws IOException {
        Path rulesRoot = locateRulesRoot();
        List<RuleDefinition> definitions = new ArrayList<>();
        try (Stream<Path> stream = Files.list(rulesRoot)) {
            // Load every category file so scoring stays driven by external rule configuration.
            for (Path ruleFile : stream
                    .filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .sorted(Comparator.naturalOrder())
                    .toList()) {
                definitions.addAll(ruleLoader.load(ruleFile).rules());
            }
        }
        return new RuleCatalog(definitions);
    }

    private Path locateRulesRoot() throws IOException {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            // Tests run from module directories, so walk upward until the shared config folder is found.
            Path candidate = current.resolve("config").resolve("rules");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IOException("Unable to locate config/rules directory from the current workspace.");
    }

    private List<RuleMatch> toRuleMatches(
            List<com.company.upgradefactory.domain.model.ScanFinding> findings,
            Map<String, RuleDefinition> rulesById
    ) {
        return findings.stream()
                .collect(Collectors.toMap(
                        com.company.upgradefactory.domain.model.ScanFinding::findingId,
                        finding -> toRuleMatch(finding, rulesById.get(finding.findingId())),
                        this::pickHigherPenaltyMatch
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingInt(RuleMatch::penaltyApplied).reversed()
                        .thenComparing(RuleMatch::ruleId))
                .toList();
    }

    private RuleMatch toRuleMatch(com.company.upgradefactory.domain.model.ScanFinding finding, RuleDefinition ruleDefinition) {
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
