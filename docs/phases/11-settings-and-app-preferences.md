# Phase 11 — Settings and App Preferences

## Status

**Phase / Branch Status:** Prepared  
**Feature / Refinement Status:** Current work in progress; developer acceptance pending

## Objective

Make Settings a reliable, understandable utility area for managing app preferences and local-data actions without disrupting the main tracking workflows.

This phase is a **refinement and reliability phase**, not permission to add unrelated features or redesign the whole app.

## Repository Analysis Baseline

### documentation in `docs/`

- TermRunway is an offline-first, privacy-focused student finance app.
- Settings remains separate from the primary navigation tabs.
- Core preferences and financial data remain local.
- The documentation in `docs/` is the source of product intent; the Android repository is the source of implementation truth.

### Android repository

The current `main` implementation already contains these Settings areas:

- **Profile:** edit and save the locally stored name.
- **Notifications & Reminders:** master notification permission/toggle, daily reminder, weekly/monthly reviews, active-plan ending alerts, and reminder time.
- **Appearance:** System, Light, and Dark theme choices.
- **Data:** export backup, restore backup with validation/preview flow, and clear local data with confirmation.
- **Categories:** add and remove custom categories.
- **About:** version and offline/privacy explanation.

The active Android branch `v1/phase/11-settings-and-app-preferences` already contains two commits beyond `main` at the time of analysis:

- `feat(phase-11): add custom reminder time`
- `feat(phase-11): refine reminders and plan progress notifications`

These changes are implementation evidence to review and validate, **not automatic proof that Phase 11 is complete**. Keep the existing branch history; do not reset or recreate the branch.

## Intended Scope

1. Review Settings information architecture and section hierarchy.
2. Make preference controls consistent, understandable, and responsive to the stored state.
3. Verify the custom reminder-time control and its persistence/scheduling behavior.
4. Verify notification permission and master/sub-toggle interactions.
5. Keep theme selection persistent and immediately reflected by the app.
6. Preserve backup/export and restore validation/confirmation behavior.
7. Keep destructive local-data clearing explicit, confirmed, and understandable.
8. Review custom-category actions and feedback.
9. Improve visual/interaction consistency where a concrete issue is observed.
10. Preserve the separate Settings screen and existing primary navigation model.

## Explicit Constraints

- No required backend, account, or internet connection.
- Financial data stays local.
- Do not introduce unrelated product features.
- Do not redesign navigation or reintroduce tracking-mode switching.
- Do not duplicate existing preference state or create a second source of truth.
- Do not describe backup, restore, notifications, or preferences as complete without checking the implementation and testing relevant behavior.
- Do not perform a broad rewrite solely to make the Settings screen look architecturally different.

## Acceptance Criteria

### Profile and preferences

- [ ] The name field rejects blank-only saves and respects the established length limit.
- [ ] Saved preferences survive app restarts.
- [ ] Theme changes apply correctly and remain selected after restart.
- [ ] Controls clearly show their selected/enabled state.

### Notifications

- [ ] On supported Android versions, notification permission is handled clearly when required.
- [ ] Denying permission does not falsely imply notifications are enabled and delivered.
- [ ] The master toggle and individual reminder toggles behave consistently.
- [ ] Custom reminder hour and minute are displayed in the device's preferred time format, persisted, and used by scheduling.
- [ ] Scheduled reminders reflect preference changes without duplicate or stale schedules.
- [ ] Any plan-progress notification behavior is consistent with the product intent and does not send misleading financial guidance.

### Local data safety

- [ ] Export uses the system document picker and creates a valid JSON backup.
- [ ] Restore validates the selected backup and provides a clear confirmation path before replacing local data.
- [ ] Invalid backups fail safely with understandable feedback.
- [ ] Clear-data requires explicit confirmation and clearly communicates the consequences.
- [ ] These actions continue to work offline.

### Categories and UX

- [ ] Duplicate/invalid custom-category names are prevented with understandable feedback.
- [ ] Settings remains separate from Home/Plan/Activity/Insights navigation.
- [ ] Layout, scrolling, keyboard behavior, accessibility labels, and touch targets are reviewed on a physical device.
- [ ] No regressions are introduced into tracking, planning, backup, or navigation workflows.

### Validation and acceptance

- [ ] Run the relevant unit tests.
- [ ] Build the debug app successfully.
- [ ] Test the changed Settings and reminder workflows on a physical Android device.
- [ ] Review changed files for unrelated scope expansion.
- [ ] Developer/product owner explicitly accepts the phase before its status is changed to Completed.

## Recommended Implementation Order

1. Review the existing Phase 11 branch changes and current implementation.
2. Validate preference persistence and notification scheduling behavior first.
3. Inspect Settings layout and controls for concrete usability problems.
4. Make focused fixes only where evidence shows a problem.
5. Run tests and build checks.
6. Perform physical-device regression testing.
7. Review implementation against every acceptance criterion.
8. Wait for explicit developer acceptance before marking the phase complete.

## Current State

The Product phase is prepared. The Android Phase 11 branch exists and contains in-progress implementation commits. The phase remains **in progress until the work is reviewed, validated, and explicitly accepted by the developer**.

## Completion

**Not completed. Developer acceptance is still required.**
