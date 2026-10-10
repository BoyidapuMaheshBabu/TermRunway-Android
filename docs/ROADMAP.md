# TermRunway Roadmap 🛣️

## Versioned Phase Rule

Phase numbers are scoped to the major product version.

- **V1:** Phase 01 → Phase 15
- **V2:** starts again at Phase 01 after explicit V1 completion/acceptance
- V1 does not continue as Phase 16.
- Post-V1 ideas remain future candidates until V2 planning assigns them a phase number.

## Roadmap Philosophy

The roadmap is organized around product value and engineering maturity, not a list of random features.

The sequence is:

**Foundation → Core workflow → Planning → Safety → Polish → Reliability → Release → Future**

## Completed / Established Direction

### Foundation

- Native Android product direction
- Offline-first architecture
- Local financial data
- Integer paise representation
- Product and navigation architecture

### Core Tracking

- Daily Tracking concept
- Transaction entry
- Income/expense model
- Categories
- Activity/history
- Home dashboard
- Insights direction

### Planning

- Plan Tracking concept
- Semester/period planning
- Expected income
- Expected expenses
- Remaining money
- Remaining time
- Actual vs plan direction
- Contextual spending guidance

### Data Safety

- Local-data philosophy
- Backup/restore direction
- JSON backup format
- Clear backup filename expectations
- Settings and app preferences

### Maturity

- UI/UX refinement
- performance/reliability work
- testing and release-readiness work
- V1 finalization

## Current Scope

The current priority is **V1 finalization**, not uncontrolled feature expansion.

The goal is a product that is:

- functionally reliable
- understandable
- visually consistent
- private
- offline-capable
- safe for local financial data
- explainable in a presentation
- maintainable by the developer

## Future Scope

Future ideas are intentionally separated from V1 requirements.

Possible future directions include:

### Product Intelligence

- smarter contextual spending guidance
- stronger actual-vs-plan explanations
- better semester progress interpretation
- useful anomaly detection without exposing financial data externally

### Planning

- richer scenario planning
- flexible recurring expectations
- improved forecasting based on the user's own local history
- more powerful semester comparisons

### Saving Goals

**Future V2 candidate — phase number not assigned yet.**

If selected when V2 planning begins, Saving Goals may become **V2 Phase 01** or another explicitly decided V2 phase. It must not be treated as V1 Phase 16.

- named savings target
- target amount and optional target date
- explicit local goal contributions
- remaining-amount and time-based guidance
- compact Home visibility with primary management inside Plan
- no new top-level navigation mode

### UX

- more polished visualization
- improved onboarding
- accessibility improvements
- refined interaction patterns based on real usage

### Data

- stronger backup workflows
- optional encrypted export/import
- migration tooling between app versions

### Platform

- broader Android device testing
- release-channel improvements
- potentially exploring other platforms only if product value justifies the complexity

## Explicitly Not a Priority

Unless a future product decision changes this:

- mandatory cloud accounts
- bank-account aggregation
- collecting financial data remotely
- unnecessary backend infrastructure
- feature expansion purely for presentation value
- complexity that does not improve the student's financial decision-making

## Roadmap Rule

A future idea does not become a requirement merely because it is technically possible.

Before adding a major feature:

1. identify the user problem
2. research it
3. evaluate privacy and complexity
4. make a decision
5. document it
6. define acceptance criteria
7. implement only if the value justifies the cost
