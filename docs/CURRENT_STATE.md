# TermRunway Current State

> This document answers: **Where is TermRunway right now?**

## Current Product

**TermRunway Android 1.0.0**

Status: **Working product / V1 finalization**

**Phase numbering:** Current cycle = **V1 Phase 01–15**. After V1 acceptance, the next cycle begins at **V2 Phase 01**.

The current implementation is maintained in:

[TermRunway-Android](https://github.com/BoyidapuMaheshBabu/TermRunway-Android)

## Current Architecture Direction

- Native Android
- Kotlin
- Jetpack Compose
- Material 3
- Offline-first
- Local data
- No required backend
- No required account
- No bank integration
- No required internet connection
- Integer paise for monetary values

## Current Product Structure

### Daily Tracking

The daily experience is designed around:

- Home dashboard
- transaction entry
- income and expense tracking
- activity/history
- insights
- categories
- date/period filtering
- useful daily and historical totals

### Plan Tracking

The planning experience is designed around:

- defined planning period
- expected income
- expected expenses
- semester-oriented planning
- remaining money
- remaining time
- money-per-day style guidance
- actual-vs-plan understanding
- contextual spending guidance

The plan experience should remain connected to the same transaction foundation used by Daily Tracking.

## Navigation Direction

The established navigation model uses:

**Home | Plan | + | Activity | Insights**

with Settings accessible separately from the main navigation.

The exact visual implementation can evolve during UI/UX refinement, but the information architecture should not change casually.

## Development Phase History

### V1 Phase Sequence

The current product-development cycle is **V1** and uses Phase 01–15. These numbers are scoped to V1.

The product has been organized into these V1 phases:

1. V1 Phase 01 — Architecture Foundation
2. V1 Phase 02 — Navigation and Home Refinement
3. V1 Phase 03 — Keyboard and Input UX
4. V1 Phase 04 — Category and Data Entry Refinement
5. V1 Phase 05 — Flexible Date Range and Period Filtering
6. V1 Phase 06 — Notifications and Smart Reminders
7. V1 Phase 07 — Home Dashboard Refinement
8. V1 Phase 08 — Plan and Semester Experience
9. V1 Phase 09 — Activity and Insights Refinement
10. V1 Phase 10 — Data Safety and Backup
11. V1 Phase 11 — Settings and App Preferences
12. V1 Phase 12 — UI/UX Polish
13. V1 Phase 13 — Performance and Reliability
14. V1 Phase 14 — Testing and Release Readiness
15. V1 Phase 15 — V1 Finalization

These phase names represent the **V1 product-development history** and should remain understandable even if implementation branches or file structures change.

After V1 completion, do not continue to Phase 16. The next major cycle restarts at **V2 Phase 01**.

## Current Priority

**Current active phase: V1 Phase 11 — Settings and App Preferences.**

V1 Phases 01–10 have been completed and developer-accepted. The current priority is now Phase 11, focused on settings and app preferences.

## README Status

The root README is intentionally **not being redesigned for every phase**.

Minor implementation work should not cause documentation churn.

A major README redesign is planned after V1 finalization so that the README represents the stable product rather than an unfinished moving target.

See [README_POLICY.md](README_POLICY.md).

## Known Product-Level Constraints

- Keep financial data local.
- Do not introduce a backend merely because one is technically convenient.
- Do not make percentages the primary planning language.
- Avoid judging spending from a single transaction without context.
- Do not add features only because they look impressive in a presentation.
- Preserve the student's mental model: money available, money spent, money planned, time remaining.

## Next Review

At the end of each major phase or release milestone, review this document and update only what has materially changed.
