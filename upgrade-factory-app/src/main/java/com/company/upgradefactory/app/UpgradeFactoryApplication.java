package com.company.upgradefactory.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.upgradefactory")
public class UpgradeFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpgradeFactoryApplication.class, args);
    }
}
