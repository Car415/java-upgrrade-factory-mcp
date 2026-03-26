package com.company.upgradefactory.app;

import com.company.upgradefactory.app.cli.AssessmentCliService;
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
        return args.length > 0 && ("scan".equals(args[0]) || "help".equals(args[0]) || "--help".equals(args[0]) || "-h".equals(args[0]));
    }

    private static void runCli(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(UpgradeFactoryApplication.class)
                .web(WebApplicationType.NONE)
                .run(args)) {
            AssessmentCliService cliService = context.getBean(AssessmentCliService.class);
            int exitCode = cliService.execute(args);
            if (exitCode != 0) {
                System.exit(exitCode);
            }
        }
    }
}
