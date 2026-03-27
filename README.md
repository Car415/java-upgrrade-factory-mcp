# Upgrade Factory MCP

Multi-module Maven starter for the Upgrade Factory MCP.
 
## Baseline
- Java 21
- Maven multi-module build

## Modules
- `upgrade-factory-domain`: shared domain model
- `upgrade-factory-scanner`: deterministic repository scanning
- `upgrade-factory-rules`: rule loading and rule evaluation 
- `upgrade-factory-scoring`: readiness scoring and rollout advice
- `upgrade-factory-report`: JSON/Markdown report generation
- `upgrade-factory-ai`: AI narrative abstraction layer
- `upgrade-factory-app`: CLI orchestration for scan and upgrade flows
- `upgrade-factory-testkit`: sample fixtures and golden test inputs

## Build
```bash
mvn clean test
```

## Build CLI jar
```bash
mvn -pl upgrade-factory-app -am package
```

## Run CLI
```bash
java -jar upgrade-factory-app/target/upgrade-factory-app-0.1.0-SNAPSHOT-jar-with-dependencies.jar scan --repo <path-to-maven-repo>
java -jar upgrade-factory-app/target/upgrade-factory-app-0.1.0-SNAPSHOT-jar-with-dependencies.jar upgrade --repo <path-to-maven-repo>
java -jar upgrade-factory-app/target/upgrade-factory-app-0.1.0-SNAPSHOT-jar-with-dependencies.jar upgrade --repo <path-to-maven-repo> --apply true
```
