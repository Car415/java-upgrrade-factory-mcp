package com.company.upgradefactory.app;

import com.company.upgradefactory.app.cli.AssessmentCliService;
import com.company.upgradefactory.app.cli.UpgradeCliService;
import com.company.upgradefactory.app.service.AssessmentApplicationService;
import com.company.upgradefactory.app.service.OpenRewriteRecipeSelector;
import com.company.upgradefactory.app.service.ProcessMavenCommandExecutor;
import com.company.upgradefactory.app.service.UpgradeApplicationService;
import com.company.upgradefactory.app.service.UpgradeExecutionPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeFactoryApplication {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeFactoryApplication.class);

    private final AssessmentCliService assessmentCliService;
    private final UpgradeCliService upgradeCliService;

    public UpgradeFactoryApplication() {
        AssessmentApplicationService assessmentApplicationService = new AssessmentApplicationService();
        this.assessmentCliService = new AssessmentCliService(assessmentApplicationService);
        this.upgradeCliService = new UpgradeCliService(new UpgradeApplicationService(
                assessmentApplicationService,
                new UpgradeExecutionPlanner(new OpenRewriteRecipeSelector()),
                new ProcessMavenCommandExecutor()
        ));
    }

    UpgradeFactoryApplication(AssessmentCliService assessmentCliService, UpgradeCliService upgradeCliService) {
        this.assessmentCliService = assessmentCliService;
        this.upgradeCliService = upgradeCliService;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new UpgradeFactoryApplication().run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    int run(String[] args) throws Exception {
        String command = args.length == 0 ? "help" : args[0];
        logger.info("Starting Upgrade Factory CLI with command '{}'", command);
        if ("upgrade".equals(command)) {
            return upgradeCliService.execute(args);
        }
        if ("scan".equals(command) || "help".equals(command) || "--help".equals(command) || "-h".equals(command)) {
            return assessmentCliService.execute(args);
        }
        logger.error("Unsupported command '{}'", command);
        assessmentCliService.execute(new String[]{"help"});
        return 1;
    }
}
