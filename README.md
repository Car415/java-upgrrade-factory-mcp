# Upgrade Factory MCP

Multi-module Maven starter for the Upgrade Factory MCP.

## Baseline
- Java 21
- Spring Boot 3.5.12
- Maven multi-module build

## Modules
- `upgrade-factory-domain`: shared domain model
- `upgrade-factory-scanner`: deterministic repository scanning
- `upgrade-factory-rules`: rule loading and rule evaluation
- `upgrade-factory-scoring`: readiness scoring and rollout advice
- `upgrade-factory-report`: JSON/Markdown report generation
- `upgrade-factory-ai`: AI narrative abstraction layer
- `upgrade-factory-app`: Spring Boot REST API
- `upgrade-factory-testkit`: sample fixtures and golden test inputs

## Build
```bash
mvn clean test
```

## Run app
```bash
mvn -pl upgrade-factory-app spring-boot:run
```
