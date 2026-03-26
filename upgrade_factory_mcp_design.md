# Upgrade Factory MCP — Design Document
 
## 1. Document Control

**Document Title:** Upgrade Factory MCP Design Document  
**Version:** 1.0  
**Status:** Draft  
**Primary Audience:** Engineering Leads, Platform Engineers, Architects, IT Managers  
**Implementation Assumption:** Maven-based build ecosystem  
**Target Runtime Scope:** Java 8 / Spring Boot 2.x to Java 21 / Spring Boot 3.x modernization

---

## 2. Executive Summary

Upgrade Factory MCP is a factory-style modernization platform designed to assess, plan, accelerate, and govern migration of Maven-based Java services from legacy runtime and framework stacks to modern supported versions, specifically Java 21 and Spring Boot 3.x.

The platform is not positioned as a generic code-generation assistant. Its primary objective is to combine deterministic scanning, structured scoring, targeted automation, and AI-assisted reasoning to support estate-wide modernization across a large microservice landscape.

The solution addresses four major needs:

1. **Discover** current repository readiness and blockers
2. **Assess** complexity, risk, and migration feasibility
3. **Transform** straightforward services with controlled automation
4. **Govern** modernization waves with clear evidence, reporting, and decision support

---

## 3. Problem Statement

In a large microservice estate, runtime modernization is not merely a code conversion task. The difficulty comes from a combination of:

- inconsistent dependency versions
- heavy framework coupling
- javax-to-jakarta migration impact
- legacy Spring Security patterns
- insufficient automated tests
- environment-specific configuration complexity
- messaging/runtime behavior risk
- lack of standardized migration assessment and reporting

Without a structured factory model, modernization becomes:

- slow
- inconsistent across teams
- difficult to estimate
- risky in production-facing services
- hard to govern at portfolio level

---

## 4. Goals

### 4.1 Primary Goals
- Standardize modernization assessment for Maven-based Java services
- Produce repeatable migration readiness scoring
- Identify blockers early and consistently
- Reduce manual engineering effort for straightforward migrations
- Improve migration planning, sequencing, and governance
- Provide repo-level and portfolio-level transparency

### 4.2 Secondary Goals
- Generate human-readable migration plans
- Produce review checklists and upgrade evidence
- Support controlled transformation workflows using deterministic tools
- Enable AI-assisted explanation and planning without replacing deterministic validation

---

## 5. Non-Goals

The following are explicitly out of scope for the initial design:

- automatic full migration of all services without human review
- replacing compile/test/runtime validation with AI judgment
- fully autonomous production deployment
- non-Maven build ecosystems in MVP
- broad refactoring unrelated to runtime/framework modernization
- replacing team ownership and architectural accountability

---

## 6. Design Principles

The design must follow these principles.

### 6.1 Deterministic Before AI
All technical discovery must be based on deterministic scanning and rule evaluation first.  
AI should interpret evidence, not invent evidence.

### 6.2 Evidence-Driven Outputs
Every migration score, blocker, or recommendation must be traceable to concrete findings such as imports, dependencies, plugins, config patterns, or test signals.

### 6.3 Standardization Over Ad Hoc Assessment
The platform should produce consistent outputs across teams and repositories through reusable rules and scoring logic.

### 6.4 Safety Over Aggressive Automation
Automated transformation should be limited to cases where the readiness tier and risk profile make automation appropriate.

### 6.5 Explainability
The system must explain:
- why a service is scored as easy, moderate, or hard
- which findings drive migration risk
- which manual validations are still required

### 6.6 Portfolio-Aware Design
The platform must support both single-repository analysis and cross-repository modernization governance.

### 6.7 Extensibility
The design must allow new rules, new migration targets, and new estate-specific checks to be added without redesigning the platform.

### 6.8 Production-Centric Realism
Compile success alone is insufficient. The platform must recognize that runtime behavior, integration testing, and deployment safety remain critical.

### 6.9 Separation of Concerns
Scanning, scoring, reasoning, transformation, and reporting must be implemented as distinct layers.

### 6.10 Incremental Delivery
The platform must be deliverable in phases, with immediate value from assessment-only capability before automated transformation is introduced.

---

## 7. High-Level Solution Overview

The platform is composed of five logical layers:

1. **Scanner Layer**
2. **Rule Engine and Scoring Layer**
3. **AI Reasoning Layer**
4. **Transformation and Execution Layer**
5. **Reporting and Governance Layer**

### 7.1 Lifecycle Model

The platform supports the following lifecycle:

- scan repository
- generate structured findings
- score readiness and complexity
- explain blockers and generate plan
- optionally perform controlled automated migration
- store and report results
- support modernization wave planning across the estate

---

## 8. Logical Architecture

```text
+---------------------------+
|   User / Engineer / Lead  |
|  Chat UI / Portal / CLI   |
+-------------+-------------+
              |
              v
+---------------------------+
|   Upgrade Factory MCP     |
|  Orchestrator / Planner   |
+-------------+-------------+
              |
   +----------+----------+----------+----------+
   |                     |                     |
   v                     v                     v
+---------+       +--------------+      +-------------+
| Scanner |       | Rule Engine  |      | AI Reasoner |
| Layer   |       | / Scoring    |      | / Summarizer|
+----+----+       +------+-------+      +------+------+
     |                   |                     |
     v                   v                     v
+-----------------------------------------------------+
| Structured Findings / Assessment Store              |
| JSON results, repo metadata, blocker catalog, score |
+------------------------+----------------------------+
                         |
                         v
+-----------------------------------------------------+
| Transformation / Execution Layer                    |
| OpenRewrite, build, test, dependency update, PR gen |
+------------------------+----------------------------+
                         |
                         v
+-----------------------------------------------------+
| Outputs                                             |
| Repo report, migration plan, reviewer checklist,    |
| portfolio dashboard, PR summary, wave recommendation|
+-----------------------------------------------------+
```

---

## 9. Component Design

## 9.1 MCP Orchestrator
Responsibilities:
- receive user request
- decide tool execution sequence
- aggregate outputs from tools
- present assessment, plan, or action outcome
- support assessment-only and transformation workflows

Example use cases:
- assess one repository
- compare multiple repositories
- generate migration plan
- trigger automated modernization
- summarize estate-wide readiness

---

## 9.2 Scanner Layer
The scanner layer performs deterministic inspection of Maven-based repositories.

### Scanner scope
- `pom.xml`
- Java source files
- configuration files
- test directories
- Dockerfiles
- deployment manifests
- framework usage signals

### Scanner categories
- current Java version
- current Spring Boot version
- current Spring Cloud version
- dependency compatibility
- `javax.*` usage
- deprecated Spring Security usage
- config complexity
- messaging/runtime complexity
- test maturity
- build/plugin modernization readiness

### Scanner output model
Scanners must return structured JSON-style findings suitable for downstream scoring and reasoning.

---

## 9.3 Rule Engine and Scoring Layer
The rule engine converts findings into:

- blockers
- warnings
- readiness score
- migration tier
- automation coverage estimate
- effort estimate

This layer provides consistency and governance.

Rules should be:
- declarative where possible
- testable
- version-aware
- easy to extend

---

## 9.4 AI Reasoning Layer
The reasoning layer must consume structured evidence and produce:

- narrative explanations
- migration strategy recommendations
- manual validation focus areas
- rollout guidance
- reviewer checklist suggestions

The AI layer must not override deterministic findings without explicit evidence.

---

## 9.5 Transformation and Execution Layer
This layer performs controlled actions such as:

- branch creation
- OpenRewrite execution
- `javax` to `jakarta` rewrite support
- dependency version updates
- Maven build execution
- test execution
- output packaging
- PR summary generation

Execution must be gated by migration tier and user intent.

---

## 9.6 Reporting and Governance Layer
Outputs should support both engineering and management consumers.

### Engineering outputs
- repo readiness report
- blocker summary
- migration plan
- build/test result summary
- PR checklist

### Management outputs
- estate readiness dashboard
- tier distribution
- blocker hotspot analysis
- recommended migration waves
- team-level modernization progress

---

## 10. Functional Requirements

### 10.1 Repository Scanning
The system shall:
- inspect Maven repositories
- extract Java and framework version signals
- identify upgrade blockers
- assess config and test maturity
- inspect messaging-related complexity where relevant

### 10.2 Scoring
The system shall:
- calculate a readiness score out of 100
- classify repositories into migration tiers
- identify top blockers
- estimate effort band
- estimate automation suitability

### 10.3 Planning
The system shall:
- generate repo-specific migration plans
- recommend validation steps
- identify manual review areas
- suggest rollout strategy

### 10.4 Transformation
The system should:
- support controlled execution of migration recipes
- run Maven build and tests after transformation
- collect and summarize failures
- produce a reviewer-ready summary

### 10.5 Reporting
The system shall:
- provide repo-level reports
- store historical results
- summarize portfolio modernization readiness
- highlight recurring blockers across repositories

---

## 11. Non-Functional Requirements

### 11.1 Reliability
Assessment results must be reproducible for the same codebase version and rule set.

### 11.2 Explainability
Every score and blocker must be traceable to findings.

### 11.3 Maintainability
Rules, scanners, and target version profiles must be easy to update.

### 11.4 Extensibility
The design must support:
- new framework targets
- new rule packs
- estate-specific checks
- additional reporting dimensions

### 11.5 Performance
Repository assessment should complete fast enough for interactive usage on a typical service repository.

### 11.6 Security
The platform must:
- avoid exposing sensitive repository content unnecessarily
- control access to repo execution actions
- clearly separate read-only assessment from write-capable transformation

### 11.7 Auditability
Assessment runs and transformation runs should be logged with:
- timestamp
- repo
- branch
- rule version
- target upgrade profile
- execution result

---

## 12. Maven-Based Assumptions

Because the target estate is Maven-based, the design assumes:

- repositories use `pom.xml`
- Java version may be defined through:
  - `maven.compiler.source`
  - `maven.compiler.target`
  - `maven.compiler.release`
  - parent/plugin settings
- dependency management may be inherited through parent POMs
- plugin versions may affect build/test behavior
- OpenRewrite and Maven plugin execution can be integrated in transformation workflows

### 12.1 Maven-Specific Checks
The scanner should inspect:
- parent POM inheritance
- dependencyManagement section
- compiler plugin
- surefire/failsafe plugin
- enforcer plugin
- shade or assembly usage
- spring-boot-maven-plugin
- custom plugins
- internal shared BOM usage

---

## 13. Readiness Scoring Model

The platform uses a 100-point readiness score where higher is better.

### 13.1 Scoring Approach
Start from 100 and deduct weighted penalties based on findings.

### 13.2 Scoring Categories

#### A. Framework Compatibility
Penalty range: 0–20  
Examples:
- very old Spring Boot version
- incompatible Spring Cloud version
- unsupported dependencies

#### B. Jakarta Migration Volume
Penalty range: 0–20  
Examples:
- widespread `javax.*` imports
- direct servlet or validation API dependency

#### C. Security Migration Complexity
Penalty range: 0–15  
Examples:
- `WebSecurityConfigurerAdapter`
- legacy security configuration model
- custom filters tied to old patterns

#### D. Messaging / Runtime Complexity
Penalty range: 0–15  
Examples:
- Spring Cloud Stream complexity
- Solace binder usage
- shared queue consumer semantics
- custom retry and error handling

#### E. Configuration Complexity
Penalty range: 0–10  
Examples:
- many environment-specific files
- bootstrap/application config ambiguity
- hardcoded endpoints
- fragile property loading patterns

#### F. Test Maturity
Penalty range: 0–10  
Examples:
- weak test coverage
- no meaningful integration tests
- absent smoke testing

#### G. Build / Deployment Complexity
Penalty range: 0–10  
Examples:
- outdated build plugins
- custom build packaging logic
- obsolete Docker base image

### 13.3 Tier Mapping
- **Tier 1 – Straightforward:** score 75–100
- **Tier 2 – Moderate:** score 50–74
- **Tier 3 – Hard:** score below 50

### 13.4 Interpretation
The score is a readiness indicator, not a release approval indicator.

---

## 14. Rollout Strategy Guidance

The platform should recommend rollout style based on service characteristics.

### Strategy A: Direct Upgrade
For low-risk internal services with strong tests.

### Strategy B: Upgrade with Isolated UAT Soak
For moderate-risk services requiring runtime validation.

### Strategy C: Side-by-Side Validation with Isolation
For critical services where old and new runtime behavior needs safe comparison.

### Strategy D: Manual Staged Cutover
For high-complexity, regulator-facing, or ordering-sensitive services.

---

## 15. Risks and Limitations

### 15.1 False Confidence Risk
AI narrative may appear more certain than the evidence supports.

**Mitigation:** Always present evidence-backed findings.

### 15.2 Runtime Unknowns
Compile/test success does not guarantee production-safe runtime behavior.

**Mitigation:** Require rollout guidance and integration validation.

### 15.3 Estate-Specific Complexity
Internal libraries and custom frameworks may not be fully covered by generic rules.

**Mitigation:** Add rule packs incrementally using internal migration learnings.

### 15.4 Over-Automation
Automated migration may create unsafe changes in high-risk services.

**Mitigation:** Gate transformation by tier, rule confidence, and explicit approval.

---

## 16. Testing Strategy

The platform itself must be tested thoroughly.

### 16.1 Unit Testing
- scanner logic
- rule evaluation
- score calculation
- target version profile resolution

### 16.2 Integration Testing
- scan representative Maven repositories
- validate parsing of real-world `pom.xml`
- test transformation workflow on sample projects

### 16.3 Golden File Testing
Use sample repositories with expected outputs to ensure scoring stability.

### 16.4 Regression Testing
When rules evolve, verify score and blocker drift intentionally.

---

## 17. Delivery Roadmap

## Phase 1: Assessment MVP
Includes:
- Maven repo scanner
- rule engine
- readiness scoring
- AI summary generation
- migration plan generation

Outcome:
- modernization triage and planning value

## Phase 2: Controlled Automation
Includes:
- OpenRewrite integration
- dependency updates
- Maven build/test execution
- PR summary generation

Outcome:
- engineering acceleration for straightforward services

## Phase 3: Portfolio Governance
Includes:
- blocker hotspot analysis
- modernization waves
- team-level reporting
- recurring pattern intelligence

Outcome:
- program-level modernization governance

---

## 18. Example MCP Tool Set

Suggested tools:
- `scan_repo_readiness`
- `score_repo_migration`
- `generate_migration_plan`
- `apply_automated_migration`
- `generate_pr_summary`
- `summarize_portfolio_upgrade`

These should remain modular and composable.

---

## 19. Recommendation

The best initial implementation path is:

1. build assessment capability first
2. prove scoring accuracy on real internal repositories
3. refine estate-specific rules
4. add controlled transformation only after confidence grows

This gives fast value while preserving engineering trust.

---

## 20. Conclusion

Upgrade Factory MCP should be built as an evidence-driven modernization platform, not merely a code upgrade assistant. Its strength comes from combining deterministic repository scanning, consistent scoring, AI-assisted explanation, and controlled automation into a single factory model suitable for Maven-based enterprise estates.

Its primary value is not only reducing migration effort, but also improving modernization governance, prioritization, transparency, and safety across a large service portfolio.
