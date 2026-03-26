package com.company.upgradefactory.app;

import com.company.upgradefactory.app.cli.AssessmentCliService;
import com.company.upgradefactory.app.cli.UpgradeCliService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.company.upgradefactory")
public class UpgradeFactoryApplication {

    public static void main(String[] args) throws Exception {
        if (isCliInvocation(args)) {
            runCli(args);
            return;
        }
        SpringApplication.run(UpgradeFactoryApplication.class, args);
    }

    private static boolean isCliInvocation(String[] args) {
        return args.length > 0 && ("scan".equals(args[0])
                || "upgrade".equals(args[0])
                || "help".equals(args[0])
                || "--help".equals(args[0])
                || "-h".equals(args[0]));
    }

    private static void runCli(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(UpgradeFactoryApplication.class)
                .web(WebApplicationType.NONE)
                .run(args)) {
            int exitCode;
            if ("upgrade".equals(args[0])) {
                UpgradeCliService cliService = context.getBean(UpgradeCliService.class);
                exitCode = cliService.execute(args);
            } else {
                AssessmentCliService cliService = context.getBean(AssessmentCliService.class);
                exitCode = cliService.execute(args);
            }
            if (exitCode != 0) {
                System.exit(exitCode);
            }
        }
    }
}
