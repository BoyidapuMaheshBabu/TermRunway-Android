# TermRunway Android — Agent Instructions

## Repository Purpose

TermRunway is an offline-first native Android student finance and semester-planning application. The repository contains the Android implementation and the durable product documentation.

## Read Before Making Changes

For meaningful work, start with:

1. [Documentation guide](docs/README.md)
2. [Product context](docs/PRODUCT_CONTEXT.md)
3. [Project progress](docs/PROGRESS.md)
4. [Product rules](docs/PRODUCT_RULES.md)
5. [Roadmap](docs/ROADMAP.md)
6. [Development workflow](docs/DEVELOPMENT_WORKFLOW.md)
7. [AI handoff](docs/AI_HANDOFF.md)

Then inspect only the relevant records in `docs/decisions/`, `docs/issues/`, and `docs/phases/`.

The documentation describes product intent and accepted constraints. Inspect the actual Android source and tests before making claims about implementation state.

## Product Invariants

- Preserve offline-first operation and local ownership of financial data.
- Do not introduce a required backend, cloud account, bank connection, or network dependency without an explicit accepted product decision.
- Preserve the two-mode model: Daily Tracking (what happened) and Plan Tracking (what will happen).
- Preserve financial correctness. Monetary values use integer paise internally; do not introduce floating-point money calculations.
- Follow the product rules and established navigation unless the requested scope explicitly changes them.
- Do not invent user data or present proposed functionality as implemented.

## Phase and Branch Discipline

- The current V1 sequence is **V1 Phase 01–15**. Do not create V1 Phase 16.
- The next planned phase is **V1 Phase 14 — Testing and Release Readiness**; V1 Phase 15 is the finalization phase.
- Saving Goals is a V2 candidate with no assigned phase number. Do not treat it as approved V1 scope.
- Use the exact canonical branch name from `docs/phases/README.md`.
- Create only the branch for the phase that is actually beginning, from the latest `main`. Do not pre-create future phase branches.
- Keep documentation maintenance and unrelated fixes on focused `chore/` or `fix/` branches, not on a product phase branch.
- Do not merge a pull request, push directly to `main`, declare a phase accepted, or create the next phase branch without explicit developer approval.

## Implementation and Validation

Before implementation, inspect the existing architecture, relevant UI, data model, repository/domain logic, tests, and build configuration. Report the problem, current behavior, proposed scope, affected areas, and acceptance criteria before a substantial change.

Keep changes focused. Preserve existing behavior unless the task explicitly changes it. Avoid speculative rewrites and unnecessary dependencies.

Use the repository's Gradle wrapper for validation:

```bash
./gradlew --no-daemon --stacktrace :app:testDebugUnitTest
./gradlew --no-daemon --stacktrace :app:assembleDebug
```

A successful build or unit-test run does not replace physical-device testing. Report exactly which checks were run and which remain outstanding. Never claim tests passed unless their results were observed.

## Documentation Rules

- `docs/decisions/` is for accepted product or engineering reasoning.
- `docs/issues/` is for unresolved questions and proposals.
- `docs/phases/` is for phase scope, acceptance criteria, and phase status.
- Update `docs/PROGRESS.md` and the relevant phase index only when an accepted change materially changes project status.
- Record durable decisions, not temporary prompts or conversation transcripts.
- When documentation and code disagree, identify the discrepancy and resolve it deliberately; do not silently rewrite product intent to match an assumption.

## Ownership

AI tools may research, explain, implement, debug, and review. The developer owns product direction, architecture acceptance, physical-device validation, phase acceptance, and release decisions.
