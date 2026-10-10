# TermRunway Documentation Index 🧭

> **The navigation hub for TermRunway's durable product knowledge.**

## 🎯 1. Product Foundation

| Document | Purpose |
|---|---|
| [Product Context](PRODUCT_CONTEXT.md) | Product identity, problem, users, modes & principles |
| [Product Rules](PRODUCT_RULES.md) | Non-negotiable product and UX rules |
| [Current State](CURRENT_STATE.md) | What is complete, current & next |
| [Roadmap](ROADMAP.md) | Product direction and future scope |
| [Development Approach](DEVELOPMENT_APPROACH.md) | How TermRunway is developed |
| [AI Handoff](AI_HANDOFF.md) | Context recovery for new AI sessions |
| [README Policy](README_POLICY.md) | When implementation documentation should change |

## 🗂️ 2. Development Phases

See [Phase Index](phases/README.md).

> **Version rule:** The current sequence is **V1 Phase 01–15**. After V1 completion/acceptance, the next major development cycle restarts at **V2 Phase 01**. Do not continue V1 with Phase 16.

| Phase | Phase / Branch Status | Feature / Refinement Status |
|---|---|---|
| V1 / 01 — Architecture Foundation | Completed | Completed |
| V1 / 02 — Navigation and Home Refinement | Completed | Completed |
| V1 / 03 — Keyboard and Input UX | Completed | Completed |
| V1 / 04 — Category and Data Entry Refinement | Completed | Completed |
| V1 / 05 — Flexible Date Range and Period Filtering | Completed | Completed |
| V1 / 06 — Notifications and Smart Reminders | Completed | Completed |
| V1 / 07 — Home Dashboard Refinement | Completed | Completed |
| V1 / 08 — Plan and Semester Experience | Completed | Completed |
| V1 / 09 — Activity and Insights Refinement | Completed | Completed |
| V1 / 10 — Data Safety and Backup | Completed | Completed |
| V1 / 11 — Settings and App Preferences | Phase prepared | In progress |
| V1 / 12 — UI/UX Polish | Phase prepared | Refinement pending |
| V1 / 13 — Performance and Reliability | Phase prepared | Hardening pending |
| V1 / 14 — Testing and Release Readiness | Phase prepared | Validation pending |
| V1 / 15 — V1 Finalization | Phase prepared | Current work pending |
| V2 candidate — Saving Goals | Proposed / not prepared | Not started |

### 🔀 Android Development Branches

The names below are the **canonical Android branch names** recorded in the documentation in `docs/` for the **V1 phase sequence**.

**V1 branch convention:** `v1/phase/<phase-number>-<phase-name>`. V2 will use `v2/phase/...`. The Android repository creates them **one at a time**, only when the corresponding phase begins.

| Phase | Canonical Android Development Branch |
|---|---|
| V1 / 01 | [v1/phase/01-architecture-foundation](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/01-architecture-foundation) |
| V1 / 02 | [v1/phase/02-navigation-and-home-refinement](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/02-navigation-and-home-refinement) |
| V1 / 03 | [v1/phase/03-keyboard-and-input-ux](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/03-keyboard-and-input-ux) |
| V1 / 04 | [v1/phase/04-category-and-data-entry-refinement](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/04-category-and-data-entry-refinement) |
| V1 / 05 | [v1/phase/05-flexible-date-range-and-period-filtering](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/05-flexible-date-range-and-period-filtering) |
| V1 / 06 | [v1/phase/06-notifications-and-smart-reminders](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/06-notifications-and-smart-reminders) |
| V1 / 07 | [v1/phase/07-home-dashboard-refinement](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/07-home-dashboard-refinement) |
| V1 / 08 | [v1/phase/08-plan-and-semester-experience](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/08-plan-and-semester-experience) |
| V1 / 09 | [v1/phase/09-activity-and-insights-refinement](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/09-activity-and-insights-refinement) |
| V1 / 10 | [v1/phase/10-data-safety-and-backup](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/10-data-safety-and-backup) |
| V1 / 11 | [v1/phase/11-settings-and-app-preferences](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/11-settings-and-app-preferences) *(created when Phase 11 begins)* |
| V1 / 12 | [v1/phase/12-ui-ux-polish](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/12-ui-ux-polish) *(created when Phase 12 begins)* |
| V1 / 13 | [v1/phase/13-performance-and-reliability](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/13-performance-and-reliability) *(created when Phase 13 begins)* |
| V1 / 14 | [v1/phase/14-testing-and-release-readiness](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/14-testing-and-release-readiness) *(created when Phase 14 begins)* |
| V1 / 15 | [v1/phase/15-v1-finalization](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/tree/v1/phase/15-v1-finalization) *(created when Phase 15 begins)* |
| 16 | — |

> **Status rule:** A Product phase can be prepared without its Android branch existing. The documentation in `docs/` records the canonical branch name; Android creates only the next active phase branch. Phase numbers are scoped to the major product version: V1 uses Phase 01–15, then V2 restarts at Phase 01. Feature / Refinement Status is determined from actual work, validation, and developer acceptance.

## 🧩 3. Decisions

See [Decision Index](decisions/README.md).

Durable decisions cover:

- 🎯 Product scope and direction
- 🧭 UX and navigation
- 🏗️ Architecture
- 🔐 Privacy and data
- 📝 Documentation and AI continuity

A decision should explain **why**, not only **what**.

## 🧩 3.5 Product Issues & Unresolved Decisions

See [Issues](issues/README.md).

Use this area for unresolved product questions, competing directions, and decisions-in-progress. Do not treat an issue as an accepted decision or implementation-ready phase.

## 🕰️ 4. Product History

- [TermRunway Android](https://github.com/BoyidapuMaheshBabu/TermRunway-Android) — current product
- [TermRunway Web](https://github.com/BoyidapuMaheshBabu/TermRunway-Web) — historical prototype

## 🛠️ 5. How to Use These Docs

### When researching
**Question → Evidence → Conclusion → Decision (if needed)**

### When planning
**Current State → Existing Decisions → Product Rules → Phase Objective → Acceptance Criteria**

### When a product question is unresolved
Use **issues/** until the question is researched and a decision is accepted.

### When implementing
Use **TermRunway-Android** as the implementation source of truth.

### When reviewing
Ask:

- Does the implementation still solve the product problem?
- Did a new durable decision appear?
- Does behavior still respect product rules?
- Does the roadmap still make sense?

## 🔁 Canonical Flow

```
Research
   ↓
Decision
   ↓
Documentation
   ↓
Phase Plan
   ↓
Android Implementation
   ↓
Real-Device Test
   ↓
Review
   ↓
Updated project documentation
```
