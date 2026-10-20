# Phase 11 — Settings, App Preferences, Data Safety & Date/Time Architecture

## Status

**Completed — developer accepted**

## Objective

Make Settings a reliable, understandable utility area while preserving the daily tracking and planning workflows. Phase 11 also addresses offline data preservation, reminder reliability, and correct calendar-date/time-zone behavior needed to keep the app trustworthy across devices and system changes.

This is a refinement and reliability phase, not permission to add unrelated product features or redesign the whole app.

## Product Constraints

- Keep TermRunway offline-first, with no required backend, account, or internet connection.
- Keep personal financial data local.
- Keep Settings separate from the primary navigation: Home | Plan | + | Activity | Insights.
- Preserve the single connected transaction foundation for Daily Tracking and Plan Tracking.
- Do not add features merely to make the project appear more sophisticated.
- Treat financial and calendar correctness as more important than visual decoration.

## Intended Scope

1. Review the Settings information architecture and section hierarchy.
2. Keep profile and preference controls clear and responsive to stored state.
3. Support the custom reminder time and ensure it is persisted and used by scheduling.
4. Keep notification permission, the master notification switch, and individual reminder controls consistent.
5. Keep System, Light, and Dark theme choices persistent.
6. Preserve export and restore safety, including validation, confirmation, progress and result feedback.
7. Keep destructive local-data clearing explicit and confirmed.
8. Preserve custom-category management and user feedback.
9. Schedule monthly review reminders against actual calendar-month boundaries, not a fixed 30-day interval.
10. Keep date-only financial values stable across time-zone changes and migrate previously stored date values safely.

## Implementation Evidence Recorded in the Phase 11 Update

### 1. Settings and preferences

- Profile name, appearance preferences, reminders, custom categories, local-data actions, and About information remain available in Settings.
- Custom reminder time supports the user's selected hour and minute.
- Notification controls include the master notification setting and individual reminder settings.
- Theme selection is stored locally.

### 2. Restore flow and user feedback

- The initial replacement dialog explains that restoring replaces current local data and summarizes the data to be replaced.
- The user can choose **Backup First** or continue to a second confirmation dialog.
- Restoration shows a blocking progress state and prevents duplicate attempts while the operation is in progress.
- Success feedback is shown after restoration reports success; failure feedback is shown when restoration fails.
- Database replacement is performed transactionally. A failed database restore should roll back rather than leave partially replaced database records.
- Repository/ViewModel state and notification schedules are refreshed after successful restoration.

### 3. Calendar-aware monthly reminders

- Month-end date calculation is extracted into `MonthlyReminderCalculator`.
- The calculation handles 28-, 29-, 30-, and 31-day months, including leap-year February, and respects the chosen reminder time.
- Monthly review scheduling uses a one-time work request for the next actual calendar-month end instead of a repeating 30-day interval.
- The next occurrence is scheduled after a monthly reminder runs.
- Existing monthly work is replaced when preferences are rescheduled, avoiding stale duplicate schedules.
- Boot and relevant system time/date/time-zone events trigger reminder rescheduling.

WorkManager timing is still subject to Android's execution constraints; a target date/time does not guarantee execution at that exact minute.

### 4. Offline backup and safe restoration

The updated backup snapshot covers:
- Profile name and portable preferences, including theme, tracking mode and reminder settings.
- Transactions and their details.
- Default and custom categories.
- Financial plans, including past/completed plans represented by the persisted plan data.
- Planned income and planned expense records.

Device-specific OS permissions and runtime notification milestone markers remain local and are not included in portable backups.

The updated backup implementation:
- Exports schema version 2 using ISO-8601 calendar-date strings.
- Retains compatibility for supported schema version 1 backups.
- Validates the app identifier, supported schema version, amounts and planned-income/expense references to existing plans.
- Restores database records transactionally and remaps plan IDs where needed while preserving child relationships.
- Re-synchronizes notifications and refreshes the in-memory state after successful restoration.

### 5. Calendar-date and time-zone correctness

- Date-only values such as transaction dates, plan start/end dates and planned-item dates use ISO-8601 strings (`YYYY-MM-DD`) and `java.time.LocalDate`.
- `createdAtMs` remains epoch milliseconds for an actual instant in time.
- The SQLite schema was upgraded from version 2 to version 3, migrating persisted date columns from legacy millisecond values to ISO date strings while preserving record IDs and amounts.
- Day counts use calendar-date arithmetic such as `ChronoUnit.DAYS.between` rather than assuming every local calendar day is exactly 86,400,000 milliseconds.
- Backup schema version 2 stores ISO date strings while supported schema version 1 backups remain importable.
- System boot, app replacement, time-setting and time-zone changes trigger notification scheduling refreshes; the app refreshes date-sensitive UI state on resume and relevant clock/date events.

## Verification Evidence

The latest Phase 11 update commit records the following validation results:

- **Automated tests:** `./gradlew test` — 63 tests passed, 0 failures.
- **Debug build:** `./gradlew assembleDebug` — reported successful.
- **Coverage specifically mentioned:** schema 1/2 backup round trips, migration from database schema 2 to 3, time-zone-invariant ISO calendar dates, leap-year and month-boundary day calculations, plan metrics, and month-end reminder calculations.

These are the results recorded by the implementation commit; they are not a claim that this reviewer independently reran the commands. The developer separately confirmed physical-device testing and acceptance of Phase 11 on 2026-10-20.

## Acceptance Coverage

The accepted scope covers:
- Profile and preferences persistence, including theme and reminder time.
- Clear notification permission and master/sub-toggle behavior.
- Monthly reminder calendar alignment and scheduling updates.
- Backup/export, restore validation, confirmation and failure safety.
- Explicit confirmation before clearing local data.
- Custom-category feedback and Settings separation from primary navigation.
- Date/time/time-zone behavior and migration of saved calendar dates.
- Relevant tests, debug build, and physical-device validation.
- No regression to tracking, planning, navigation, financial calculations or offline behavior.

## Historical Review Notes

Earlier review notes described potential or observed concerns in restore feedback, monthly reminder scheduling, backup completeness, and date/time handling. The Phase 11 update commit records implementation work and verification evidence addressing these areas. Those earlier notes must not be treated as currently open defects merely because they remain in Git history.

If a new reproducible issue is found later, document it through the project's issue workflow and assign it to the appropriate phase. Do not reopen accepted Phase 11 work without current evidence.

## Completion

**Completed — developer acceptance recorded on 2026-10-20 following physical-device testing.**

Preserve `v1/phase/11-settings-and-app-preferences` as the accepted Phase 11 historical checkpoint once the latest accepted update is merged into `main`.
