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


## Review Findings

### Issue 1 — Backup restore feedback and navigation order

**Status:** Open — observed during manual testing; root cause still needs confirmation.

**Observed behavior**
- Restore feedback appears after the app has navigated to another screen, rather than following a clear restore-result flow.
- In `SettingsScreen.kt`, `onRestore(uri)` is called and `showSuccessDialog` is set immediately afterward. If restoration completes asynchronously, this could show success before the operation has actually succeeded.

**Expected behavior**
1. Show progress while the backup is being restored.
2. Show success only after restoration completes successfully, or show an error if it fails.
3. After the user acknowledges success, navigate to Home and display the restored data.

**Recommended direction**
- Expose or reuse a clear restore state/result from the existing restore operation: in progress, success, or failure.
- Show the matching feedback and navigate only after successful completion is confirmed and the user acknowledges it.

**Validation**
- Test a valid backup and an invalid/corrupted backup. Success must never appear before a successful restore.
- This records the observed behavior and proposed direction; it does not claim the issue is fixed.

### Issue 2 — Monthly reminder must follow calendar months

**Status:** Open — calendar-month behavior agreed; implementation needs correction.

**Confirmed implementation mismatch**
- `NotificationScheduler.kt` uses `PeriodicWorkRequestBuilder<MonthlyReviewWorker>(30, TimeUnit.DAYS)`, so repeat reminders use a fixed 30-day interval.
- The initial delay calculation sets the target day to day 1 of the month, which also conflicts with the Settings description: “Remind at the end of the month to review monthly spending.”
- Therefore, the current implementation does **not** reliably follow real calendar months or month-end dates.

**Required behavior**
- Schedule the monthly review according to actual calendar-month boundaries and the user's selected reminder time.
- Handle months with 28, 29, 30, and 31 days correctly. For example, a month-end reminder should fall on February's last day, then March's last day, then April's last day.
- Do not use a repeating 30-day interval as a substitute for calendar-month scheduling.

**Recommended direction**
- Use calendar-aware one-time scheduling for the next intended month-end reminder, then schedule the next occurrence after it runs, or use another approach that reliably recalculates each calendar month's target.
- Respect Android WorkManager's normal execution timing constraints; calendar alignment does not guarantee exact-to-the-minute delivery.

**Validation**
- Verify the calculated target date and selected local time across short and long months, leap-year February, time changes, and preference changes.
- Confirm disabling the monthly reminder cancels pending work and re-enabling it schedules the correct next calendar occurrence.
- This is a documented requirement and finding, not an implementation fix.

### Issue 3 — Define and guarantee a complete offline user-data backup

**Status:** Open — product requirement clarified; implementation coverage and restore behavior need a complete audit.

**Product intent**
TermRunway is offline-first. A backup must be a portable local file that lets a user preserve and restore their meaningful app data on the same device or another compatible installation, without logging in or fetching data from a cloud server. Exporting a backup is a snapshot of the user's current data; it is **not** intended to record every edit as a separate transaction version or create an audit log of every interaction.

**Data that should be covered**
- The user's saved name/profile details.
- Transactions, including their current saved values and transaction details.
- Categories, including custom categories and the information needed to retain their type and identity/meaning.
- Financial plans and their plan inputs, including planned incomes and planned expenses.
- Plan history, such as completed or past plans and their related data, if the app persists that history separately or could otherwise omit it.
- Any other persisted user-created financial data that is necessary to reconstruct the user's experience after restore.
- Relevant app preferences only where product intent says they should travel with the backup; document clearly which preferences are included and which remain device-specific.

**Current implementation evidence**
- `BackupManager.kt` currently serializes the name, theme mode, transactions, plans, planned incomes, planned expenses, and categories.
- That is useful existing coverage, but it is not enough by itself to prove that all meaningful user data and plan history are included. The persisted data model and restore path must be checked to determine whether past/completed plans and any separately stored history are already represented.
- The restore warning must reflect all replaceable local data, not only transactions and plans. For example, custom categories or profile data must not be silently overwritten just because no transactions exist.

**Required behavior**
1. Define an explicit backup data inventory from the app's persisted models and repositories.
2. Ensure export writes a consistent snapshot of all in-scope data into the local JSON backup.
3. Ensure restore validates the file and restores all in-scope records and relationships together, preserving necessary IDs/references or safely remapping them.
4. Ensure a failed/invalid restore does not leave the app in a partially replaced state.
5. Explain before confirmation that restore replaces the corresponding local data; never imply that data is being downloaded from a cloud account.
6. Keep export and restore fully usable offline through the system document picker.
7. Treat each backup as the current snapshot. Do not add per-edit transaction history unless a separate product requirement is agreed.

**Recommended direction**
- Audit the persistence models, export serialization, parser/validation, and restore transaction as one end-to-end feature.
- Add a versioned JSON schema and tests that round-trip a representative dataset containing profile name, default/custom categories, transactions, active and completed/past plans, planned incomes/expenses, and any persisted plan-history records.
- Include regression tests for empty datasets, category-only/profile-only data, missing optional fields in supported older schema versions, invalid files, and restore failure safety.
- Verify whether plan history is stored as separate records or is represented by the existing plans/transactions. Do not invent a separate history store if the current product model does not need one.

**Validation**
- Export a representative dataset, inspect the JSON contents, restore it into a clean test state, and compare all in-scope data and relationships before/after.
- Confirm that custom categories and user name are restored even when there are no transactions.
- Confirm that completed/past plan history is preserved wherever it exists in the data model.
- Confirm that the entire flow works offline and that restoring never depends on a cloud login or server.
- This is a scope/requirements finding; it does not claim the current implementation is missing every listed field or that any code has been fixed.

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
