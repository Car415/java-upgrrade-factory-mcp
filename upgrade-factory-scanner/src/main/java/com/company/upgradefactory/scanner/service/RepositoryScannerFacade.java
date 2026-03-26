package com.company.upgradefactory.scanner.service;

import com.company.upgradefactory.domain.enums.FindingCategory;
import com.company.upgradefactory.domain.enums.Severity;
import com.company.upgradefactory.domain.model.RepoDescriptor;
import com.company.upgradefactory.domain.model.ScanFinding;
import com.company.upgradefactory.scanner.maven.PomScanner;
import com.company.upgradefactory.scanner.source.JavaImportScanner;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class RepositoryScannerFacade {

    private final PomScanner pomScanner = new PomScanner();
    private final JavaImportScanner javaImportScanner = new JavaImportScanner();

    public List<ScanFinding> scan(RepoDescriptor descriptor) throws IOException {
        List<ScanFinding> findings = new ArrayList<>();
        Path repoPath = Path.of(descriptor.repoPath());
        pomScanner.scan(repoPath.resolve("pom.xml")).ifPresent(model -> addPomFindings(findings, model));
        addSourceFindings(findings, repoPath);
        addConfigurationFindings(findings, repoPath);
        addTestFindings(findings, repoPath);
        return findings;
    }

    private void addPomFindings(List<ScanFinding> findings, Model model) {
        Properties properties = model.getProperties();
        String javaVersion = resolveJavaVersion(properties);
        if (isOlderThan(javaVersion, 21)) {
            findings.add(new ScanFinding(
                    "BUILD-001",
                    FindingCategory.BUILD,
                    Severity.MEDIUM,
                    "Java version is below the Java 21 target baseline.",
                    List.of(javaVersion),
                    List.of("pom.xml"),
                    Map.of("javaVersion", javaVersion)
            ));
        }

        if (hasInheritedVersionConstraints(model)) {
            findings.add(new ScanFinding(
                    "BUILD-005",
                    FindingCategory.BUILD,
                    Severity.HIGH,
                    "Parent POM or dependency management may constrain the runtime upgrade.",
                    collectPomConstraintEvidence(model),
                    List.of("pom.xml"),
                    Map.of("hasParent", model.getParent() != null,
                            "managedDependencyCount", model.getDependencyManagement() == null
                                    ? 0
                                    : model.getDependencyManagement().getDependencies().size())
            ));
        }

        resolveSpringBootVersion(model).filter(version -> isOlderThan(version, 2, 7)).ifPresent(version ->
                findings.add(new ScanFinding(
                        "FW-001",
                        FindingCategory.FRAMEWORK,
                        Severity.HIGH,
                        "Spring Boot version is below the recommended compatibility bridge.",
                        List.of(version),
                        List.of("pom.xml"),
                        Map.of("springBootVersion", version)
                )));

        resolveSpringCloudVersion(model).ifPresent(version ->
                findings.add(new ScanFinding(
                        "FW-002",
                        FindingCategory.FRAMEWORK,
                        Severity.HIGH,
                        "Spring Cloud version requires compatibility validation for the Boot 3 target.",
                        List.of(version),
                        List.of("pom.xml"),
                        Map.of("springCloudVersion", version)
                )));

        if (hasDependency(model, "org.springframework.cloud", "spring-cloud-stream")) {
            findings.add(new ScanFinding(
                    "MSG-001",
                    FindingCategory.MESSAGING,
                    Severity.MEDIUM,
                    "Spring Cloud Stream usage detected in Maven dependencies.",
                    List.of("org.springframework.cloud:spring-cloud-stream"),
                    List.of("pom.xml"),
                    Map.of()
            ));
        }
    }

    private void addSourceFindings(List<ScanFinding> findings, Path repoPath) throws IOException {
        Path sourceRoot = repoPath.resolve("src/main/java");
        List<String> javaxImports = javaImportScanner.findImports(sourceRoot, "javax.");
        if (javaxImports.stream().anyMatch(name -> name.startsWith("javax.servlet"))) {
            findings.add(new ScanFinding(
                    "JAK-001",
                    FindingCategory.JAKARTA,
                    Severity.HIGH,
                    "javax.servlet imports detected and require migration to jakarta.servlet.",
                    javaxImports.stream().filter(name -> name.startsWith("javax.servlet")).toList(),
                    List.of(sourceRoot.toString()),
                    Map.of("count", javaxImports.size())
            ));
        }
        if (javaxImports.size() >= 3) {
            findings.add(new ScanFinding(
                    "JAK-004",
                    FindingCategory.JAKARTA,
                    Severity.HIGH,
                    "Widespread javax imports indicate a larger Jakarta migration surface.",
                    javaxImports,
                    List.of(sourceRoot.toString()),
                    Map.of("count", javaxImports.size())
            ));
        }

        List<String> securityConfigurerImports = javaImportScanner.findImports(
                sourceRoot,
                "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter"
        );
        if (!securityConfigurerImports.isEmpty()) {
            findings.add(new ScanFinding(
                    "SEC-001",
                    FindingCategory.SECURITY,
                    Severity.HIGH,
                    "WebSecurityConfigurerAdapter usage detected.",
                    securityConfigurerImports,
                    List.of(sourceRoot.toString()),
                    Map.of("count", securityConfigurerImports.size())
            ));
        }

        List<String> cloudStreamImports = javaImportScanner.findImports(sourceRoot, "org.springframework.cloud.stream");
        if (!cloudStreamImports.isEmpty()) {
            findings.add(new ScanFinding(
                    "MSG-001",
                    FindingCategory.MESSAGING,
                    Severity.MEDIUM,
                    "Spring Cloud Stream imports detected in the codebase.",
                    cloudStreamImports,
                    List.of(sourceRoot.toString()),
                    Map.of("count", cloudStreamImports.size())
            ));
        }
    }

    private void addConfigurationFindings(List<ScanFinding> findings, Path repoPath) throws IOException {
        Path resourcesRoot = repoPath.resolve("src/main/resources");
        if (!Files.exists(resourcesRoot)) {
            return;
        }

        List<Path> configFiles;
        try (Stream<Path> stream = Files.walk(resourcesRoot)) {
            configFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.endsWith(".yml")
                                || fileName.endsWith(".yaml")
                                || fileName.endsWith(".properties");
                    })
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }

        List<String> bootstrapFiles = configFiles.stream()
                .map(path -> path.getFileName().toString())
                .filter(fileName -> fileName.startsWith("bootstrap."))
                .toList();
        if (!bootstrapFiles.isEmpty()) {
            findings.add(new ScanFinding(
                    "CFG-001",
                    FindingCategory.CONFIG,
                    Severity.LOW,
                    "bootstrap configuration files detected.",
                    bootstrapFiles,
                    bootstrapFiles,
                    Map.of("count", bootstrapFiles.size())
            ));
        }

        List<String> hardcodedConfigLines = new ArrayList<>();
        for (Path configFile : configFiles) {
            // Keep the heuristic intentionally simple for the MVP: surface obvious environment-bound values.
            List<String> lines = Files.readAllLines(configFile);
            for (int index = 0; index < lines.size(); index++) {
                String normalized = lines.get(index).toLowerCase();
                if (normalized.contains("localhost")
                        || normalized.contains("127.0.0.1")
                        || normalized.contains("http://")
                        || normalized.contains("https://")) {
                    hardcodedConfigLines.add(configFile.getFileName() + ":" + (index + 1) + "=" + lines.get(index).trim());
                }
            }
        }

        if (!hardcodedConfigLines.isEmpty()) {
            findings.add(new ScanFinding(
                    "CFG-003",
                    FindingCategory.CONFIG,
                    Severity.MEDIUM,
                    "Potential hardcoded environment-specific configuration values detected.",
                    hardcodedConfigLines.stream().limit(5).toList(),
                    configFiles.stream().map(Path::toString).toList(),
                    Map.of("count", hardcodedConfigLines.size())
            ));
        }

        boolean sharedQueueConfig = hardcodedConfigLines.stream().anyMatch(line -> line.toLowerCase().contains("queue"))
                || configFiles.stream().anyMatch(path -> path.getFileName().toString().toLowerCase().contains("queue"));
        if (sharedQueueConfig) {
            findings.add(new ScanFinding(
                    "MSG-003",
                    FindingCategory.MESSAGING,
                    Severity.HIGH,
                    "Queue-centric runtime configuration detected and should be isolated during rollout.",
                    hardcodedConfigLines.stream().filter(line -> line.toLowerCase().contains("queue")).limit(3).toList(),
                    configFiles.stream().map(Path::toString).toList(),
                    Map.of()
            ));
        }
    }

    private void addTestFindings(List<ScanFinding> findings, Path repoPath) throws IOException {
        Path testRoot = repoPath.resolve("src/test/java");
        long testFileCount = countJavaFiles(testRoot);
        if (testFileCount == 0) {
            findings.add(new ScanFinding(
                    "TEST-001",
                    FindingCategory.TEST,
                    Severity.HIGH,
                    "No unit tests were detected under src/test/java.",
                    List.of(),
                    List.of(testRoot.toString()),
                    Map.of("count", 0)
            ));
            return;
        }

        if (testFileCount < 2) {
            findings.add(new ScanFinding(
                    "TEST-002",
                    FindingCategory.TEST,
                    Severity.HIGH,
                    "Only limited automated test coverage was detected.",
                    List.of("Detected " + testFileCount + " test source file(s)."),
                    List.of(testRoot.toString()),
                    Map.of("count", testFileCount)
            ));
        }
    }

    private long countJavaFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return 0;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .count();
        }
    }

    private String resolveJavaVersion(Properties properties) {
        return properties.getProperty(
                "maven.compiler.release",
                properties.getProperty(
                        "maven.compiler.target",
                        properties.getProperty("java.version", "unknown")
                )
        );
    }

    private boolean hasInheritedVersionConstraints(Model model) {
        return model.getParent() != null
                || (model.getDependencyManagement() != null
                && !model.getDependencyManagement().getDependencies().isEmpty());
    }

    private List<String> collectPomConstraintEvidence(Model model) {
        List<String> evidence = new ArrayList<>();
        Parent parent = model.getParent();
        if (parent != null) {
            evidence.add(parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion());
        }
        if (model.getDependencyManagement() != null) {
            model.getDependencyManagement().getDependencies().stream()
                    .map(dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId())
                    .forEach(evidence::add);
        }
        return evidence;
    }

    private java.util.Optional<String> resolveSpringBootVersion(Model model) {
        if (model.getParent() != null && "spring-boot-starter-parent".equals(model.getParent().getArtifactId())) {
            return java.util.Optional.ofNullable(model.getParent().getVersion());
        }
        return model.getDependencies().stream()
                .filter(dependency -> "org.springframework.boot".equals(dependency.getGroupId()))
                .map(dependency -> dependency.getVersion())
                .filter(version -> version != null && !version.isBlank())
                .findFirst();
    }

    private java.util.Optional<String> resolveSpringCloudVersion(Model model) {
        return model.getDependencies().stream()
                .filter(dependency -> "org.springframework.cloud".equals(dependency.getGroupId()))
                .map(dependency -> dependency.getVersion())
                .filter(version -> version != null && !version.isBlank())
                .findFirst();
    }

    private boolean hasDependency(Model model, String groupId, String artifactPrefix) {
        return model.getDependencies().stream()
                .anyMatch(dependency -> groupId.equals(dependency.getGroupId())
                        && dependency.getArtifactId() != null
                        && dependency.getArtifactId().startsWith(artifactPrefix));
    }

    private boolean isOlderThan(String version, int majorTarget) {
        return isOlderThan(version, majorTarget, 0);
    }

    private boolean isOlderThan(String version, int majorTarget, int minorTarget) {
        int[] parsed = parseVersion(version);
        if (parsed[0] != majorTarget) {
            return parsed[0] < majorTarget;
        }
        return parsed[1] < minorTarget;
    }

    private int[] parseVersion(String version) {
        if (version == null || version.isBlank()) {
            return new int[]{0, 0};
        }
        String normalized = version.trim();
        if (normalized.startsWith("1.")) {
            normalized = normalized.substring(2);
        }
        String[] segments = normalized.split("[.-]");
        int major = parseInt(segments, 0);
        int minor = parseInt(segments, 1);
        return new int[]{major, minor};
    }

    private int parseInt(String[] segments, int index) {
        if (index >= segments.length) {
            return 0;
        }
        try {
            return Integer.parseInt(segments[index]);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
