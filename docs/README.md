# TermRunway Product & Engineering Documentation 🧭

> **Your money. Your semester. Your runway.**

This directory contains TermRunway's durable product context, rules, decisions, unresolved questions, development phases, roadmap, and workflow. It lives alongside the Android implementation so product intent and implementation history remain connected.

For repository-level AI instructions, start with [AGENTS.md](../AGENTS.md).

## Current V1 State

**TermRunway Android 1.0.0** is the active offline-first product.

- **Last accepted phase:** V1 Phase 13 — Performance and Reliability.
- **Next:** V1 Phase 14 — Testing and Release Readiness.
- **After that:** V1 Phase 15 — V1 Finalization.
- **Future candidate:** Saving Goals is recorded as a V2 candidate; it has no assigned V2 phase and is not V1 scope.

The canonical phase and branch status is maintained in [phases/README.md](phases/README.md). The current product and implementation checkpoint is maintained in [PROGRESS.md](PROGRESS.md).

## Start Here

| Document | Purpose |
|---|---|
| [PRODUCT_CONTEXT.md](PRODUCT_CONTEXT.md) | Product identity, user problem, modes, and principles |
| [PROGRESS.md](PROGRESS.md) | Current product state, accepted work, and next phase |
| [PRODUCT_RULES.md](PRODUCT_RULES.md) | Non-negotiable product and UX rules |
| [ROADMAP.md](ROADMAP.md) | Current direction and future candidates |
| [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md) | Research, planning, implementation, and validation workflow |
| [AI_HANDOFF.md](AI_HANDOFF.md) | Context recovery and analysis-first guidance for AI sessions |
| [README_POLICY.md](README_POLICY.md) | When the root README should change |
| [phases/README.md](phases/README.md) | Canonical phase status and branch mapping |
| [decisions/README.md](decisions/README.md) | Accepted product and engineering decisions |
| [issues/README.md](issues/README.md) | Unresolved product questions and future candidates |
| [PPT.md](PPT.md) | Presentation content reference |

## Documentation Responsibilities

- **Product context and rules** explain what TermRunway is and how it should behave.
- **decisions/** records accepted reasoning and the consequences of product or engineering choices.
- **issues/** preserves questions that are not yet approved decisions or implementation-ready scope.
- **phases/** defines focused implementation and validation units.
- **PROGRESS.md** records the current accepted state and the next development objective.
- **assets/** contains documentation and branding assets used by the repository.

Do not use documentation to imply that proposed or planned functionality is already implemented.

## Repository Boundaries

- **Repository root and `app/`:** Android source, tests, build configuration, and release work.
- **`docs/`:** durable product knowledge, decisions, phase plans, unresolved questions, and development guidance.

The Android source is the implementation source of truth. Product documentation explains intent and accepted constraints. When they disagree, inspect the implementation and surface the discrepancy rather than silently rewriting either side.

## Development Loop

```text
Problem → Research → Decision → Documentation → Phase Plan
       → Android Implementation → Build/Test → Device Validation → Review
```

AI may assist with research and implementation. The developer owns product decisions, physical-device acceptance, and shipping decisions.

---

**Understand first. Decide clearly. Build efficiently. Validate the real product. Preserve what matters. 🚀**
