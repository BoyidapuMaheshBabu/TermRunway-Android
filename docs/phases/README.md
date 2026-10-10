# Development Phases 🗂️

> TermRunway is developed through **focused, product-level phases** rather than one large undifferentiated backlog.
>
> **Current cycle: V1 Phase 01–15. After V1 completion/acceptance, numbering restarts at V2 Phase 01.**

## 📊 Canonical Phase Status

| V1 Phase | Phase / Branch Status | Feature / Refinement Status |
|---|---|---|
| V1 / 01 — Architecture Foundation | ✅ Completed | ✅ Completed |
| V1 / 02 — Navigation and Home Refinement | ✅ Completed | ✅ Completed |
| V1 / 03 — Keyboard and Input UX | ✅ Completed | ✅ Completed |
| V1 / 04 — Category and Data Entry Refinement | ✅ Completed | ✅ Completed |
| V1 / 05 — Flexible Date Range and Period Filtering | ✅ Completed | ✅ Completed |
| V1 / 06 — Notifications and Smart Reminders | ✅ Completed | ✅ Completed |
| V1 / 07 — Home Dashboard Refinement | ✅ Completed | ✅ Completed |
| V1 / 08 — Plan and Semester Experience | ✅ Completed | ✅ Completed |
| V1 / 09 — Activity and Insights Refinement | ✅ Completed | ✅ Completed |
| V1 / 10 — Data Safety and Backup | ✅ Completed | ✅ Completed |
| V1 / 11 — Settings and App Preferences | ✅ Phase prepared | 🚧 In progress |
| V1 / 12 — UI/UX Polish | ✅ Phase prepared | ⏳ Refinement pending |
| V1 / 13 — Performance and Reliability | ✅ Phase prepared | ⏳ Hardening pending |
| V1 / 14 — Testing and Release Readiness | ✅ Phase prepared | ⏳ Validation pending |
| V1 / 15 — V1 Finalization | ✅ Phase prepared | 🚧 Current work pending |
| V2 candidate — Saving Goals | ⏳ Proposed / not prepared | ⏳ Not started |

## 🔀 V1 Phase / Branch Mapping

The names below are the **canonical Android branch names recorded by the documentation in `docs/` for V1**. They are created in the Android repository **one phase at a time**, only when development of that phase begins.

**V1 branch convention:** `v1/phase/<phase-number>-<phase-name>`. V2 will use `v2/phase/...`.

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

### 🧾 Status Interpretation

TermRunway intentionally separates **phase preparation** from **actual feature or refinement completion**.

- **Phase / Branch Status** describes whether the Product phase has been **prepared and accepted as a development unit**.
- **Feature / Refinement Status** describes whether the **actual work defined by that phase** has been implemented, validated, and accepted.
- A prepared Product phase does **not** mean its Android branch already exists.
- Android phase branches are created **one at a time**, only when the corresponding phase begins.
- Completed phase branches are preserved for reference and are not updated with later `main` changes.
- A phase may contain refinement, hardening, optimization, edge-case handling, UX polish, or validation without introducing a new feature.
- AI must verify actual implementation/history before reporting Feature / Refinement Status.

The current phase statuses above are the canonical AI↔developer communication state for **V1**.

V2 will receive a new phase sequence beginning at **V2 Phase 01** after V1 completion.

## 🧭 Phase Lifecycle

```
Define
  ↓
Research
  ↓
Decide
  ↓
Document
  ↓
Implement
  ↓
Test
  ↓
Review
  ↓
Developer Acceptance
  ↓
Close
```

## 📌 Documentation Rule

Create a phase document when the phase contains durable:

- product requirements
- acceptance criteria
- architecture decisions
- UX decisions
- research conclusions
- important trade-offs

A small implementation change does **not** need a product document.

> **The goal is traceability, not bureaucracy.**

### 🔢 Version Boundary

**V1 Phase 15 — V1 Finalization** is the final phase in the current V1 sequence. There is intentionally no V1 Phase 16. Post-V1 ideas remain future candidates until V2 planning begins.

### 🔒 Completion Authority

A phase may be technically ready before it is officially complete.

**The developer/product owner explicitly decides when a phase is complete.** AI must not change a phase to **Completed** merely because implementation, tests, builds, a PR, or device validation succeeded.

When the developer explicitly states that the phase is completed/accepted, that statement authorizes the AI to update the phase status and any directly affected canonical status documents.

> **Technical readiness is evidence. Developer acceptance is completion.**
