package com.company.upgradefactory.app.service;

import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.dto.CommandExecutionResult;
import com.company.upgradefactory.app.dto.UpgradeExecutionPlan;
import com.company.upgradefactory.app.dto.UpgradeMode;
import com.company.upgradefactory.app.dto.UpgradeReport;
import com.company.upgradefactory.domain.model.AssessmentResult;
import com.company.upgradefactory.domain.model.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UpgradeApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeApplicationService.class);
    private final AssessmentApplicationService assessmentApplicationService;
    private final UpgradeExecutionPlanner upgradeExecutionPlanner;
    private final MavenCommandExecutor mavenCommandExecutor;

    public UpgradeApplicationService() {
        this(new AssessmentApplicationService(),
                new UpgradeExecutionPlanner(new OpenRewriteRecipeSelector()),
                new ProcessMavenCommandExecutor());
    }

    public UpgradeApplicationService(
            AssessmentApplicationService assessmentApplicationService,
            UpgradeExecutionPlanner upgradeExecutionPlanner,
            MavenCommandExecutor mavenCommandExecutor
    ) {
        this.assessmentApplicationService = assessmentApplicationService;
        this.upgradeExecutionPlanner = upgradeExecutionPlanner;
        this.mavenCommandExecutor = mavenCommandExecutor;
    }

    public UpgradeReport executeUpgrade(Path repoPath, String repoName, String branch, UpgradeMode mode)
            throws IOException, InterruptedException {
        logger.info("Preparing upgrade execution for '{}' in {} mode", repoName, mode);
        AssessmentResult before = assess(repoPath, repoName, branch);
        UpgradeExecutionPlan plan = upgradeExecutionPlanner.plan(before, mode);
        logger.info("Selected {} recipe(s) for '{}': {}", plan.selectedRecipes().size(), repoName, plan.selectedRecipes());
        if (mode == UpgradeMode.DRY_RUN || !plan.applyAllowed()) {
            logger.info("Returning dry-run report for '{}' without executing Maven commands", repoName);
            return new UpgradeReport(
                    repoName,
                    repoPath.toString(),
                    mode,
                    before,
                    plan,
                    List.of(),
                    null,
                    before.blockers().stream().map(RuleMatch::recommendation).toList(),
                    plan.verificationCommands(),
                    "Upgrade dry-run prepared for %s with %d selected recipe(s)."
                            .formatted(repoName, plan.selectedRecipes().size())
            );
        }

        List<CommandExecutionResult> commandResults = new ArrayList<>();
        commandResults.add(executeCommand(repoPath, plan.executionCommands().getFirst(), "rewrite"));
        for (String verificationCommand : plan.verificationCommands()) {
            commandResults.add(executeCommand(repoPath, verificationCommand, "verification"));
        }
        AssessmentResult after = assess(repoPath, repoName, branch);
        logger.info("Upgrade apply flow finished for '{}' with {} command result(s)", repoName, commandResults.size());
        String summary = buildApplySummary(repoName, commandResults);
        return new UpgradeReport(
                repoName,
                repoPath.toString(),
                mode,
                before,
                plan,
                commandResults,
                after,
                after.blockers().stream().map(RuleMatch::recommendation).toList(),
                plan.verificationCommands(),
                summary
        );
    }

    private AssessmentResult assess(Path repoPath, String repoName, String branch) throws IOException {
        return assessmentApplicationService.assessResult(new AssessmentRequest(
                repoName,
                repoPath.toString(),
                branch,
                "21",
                "3.5.12"
        ));
    }

    private CommandExecutionResult executeCommand(Path repoPath, String rawCommand, String stage)
            throws IOException, InterruptedException {
        List<String> command = List.of(rawCommand.split(" "));
        logger.debug("Executing {} stage command: {}", stage, rawCommand);
        CommandExecutionResult result = mavenCommandExecutor.execute(repoPath, command);
        return new CommandExecutionResult(
                stage,
                result.exitCode(),
                result.standardOutput(),
                result.standardError(),
                result.successful()
        );
    }

    private String buildApplySummary(String repoName, List<CommandExecutionResult> commandResults) {
        CommandExecutionResult rewriteResult = commandResults.stream()
                .filter(result -> "rewrite".equals(result.stage()))
                .findFirst()
                .orElse(null);
        if (rewriteResult != null && !rewriteResult.successful()) {
            String output = rewriteResult.standardOutput() + System.lineSeparator() + rewriteResult.standardError();
            if (output.contains("No plugin found for prefix 'rewrite'")
                    || output.contains("PluginResolutionException")
                    || output.contains("Could not find artifact org.openrewrite")) {
                return "Rewrite plugin or recipe resolution failed for %s. Review the generated command and ensure Rewrite plugin coordinates and recipe artifacts are resolvable."
                        .formatted(repoName);
            }
        }
        boolean verificationFailed = commandResults.stream()
                .filter(result -> "verification".equals(result.stage()))
                .anyMatch(result -> !result.successful());
        if (verificationFailed) {
            return "Upgrade apply executed for %s, but compile or test verification failed. Review the generated changes and continue manual remediation."
                    .formatted(repoName);
        }
        return "Upgrade apply completed for %s with %d command(s) executed."
                .formatted(repoName, commandResults.size());
    }
}
