# API Contracts

## POST /api/v1/assess
Accepts a repository path and target versions, then returns a readiness assessment.

## POST /api/v1/plan
Accepts an assessment summary and returns a migration plan.

Example request body:

```json
{
  "repoName": "sample-service-a",
  "readinessScore": 68,
  "migrationTier": "TIER_2_MODERATE",
  "rolloutStrategy": "ISOLATED_UAT_SOAK",
  "blockers": [
    "Refactor to jakarta.servlet and re-test servlet behavior.",
    "Review inherited dependency management and decouple old version locks."
  ]
}
```

Example response shape:

```json
{
  "repoName": "sample-service-a",
  "migrationTier": "TIER_2_MODERATE",
  "rolloutStrategy": "ISOLATED_UAT_SOAK",
  "executionPhases": [
    "Baseline the repository with a clean build, dependency inventory, and current test signal capture."
  ],
  "firstActions": [
    "Address blocker: Refactor to jakarta.servlet and re-test servlet behavior."
  ],
  "validationFocusAreas": [
    "Review servlet, validation, and annotation namespace changes for Jakarta compatibility."
  ],
  "summary": "Migration plan for sample-service-a targets a TIER_2_MODERATE execution path with ISOLATED_UAT_SOAK rollout. Readiness score is 68 and the plan is prioritized around 2 known blockers."
}
```
