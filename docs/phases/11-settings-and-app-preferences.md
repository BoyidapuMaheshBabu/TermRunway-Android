# Phase 11: Settings, App Preferences & Date/Time Architecture

## Overview
Phase 11 addresses user experience, data safety, notification reliability, and date/time/timezone architecture improvements in TermRunway Android:
1. Restore feedback and navigation order.
2. Calendar-aware monthly reminders.
3. Complete offline backup and safe restoration.
4. Robust device date, time, and time-zone architecture.

---

## Issue 1: Correct the Restore Experience

### Implementation Details
- **First Dialog (`Replace Current Data?`):**
  - Prompt: Warns the user that restoring will replace local data and suggests backing up first.
  - Backup summary details: Profile name, transaction count, plan count, planned incomes count, planned expenses count, categories count.
  - Controls: Close (`✕`) control at top-right to dismiss dialog. Two action buttons: `Backup First` and `Continue`.
  - Behavior: Tapping `Backup First` triggers the system document picker (`CreateDocument`) to export current local data while preserving the pending restore state. Tapping `Continue` navigates to the second confirmation dialog without starting restoration.
- **Second Dialog (`Confirm Restore`):**
  - Prompt: Asks for final confirmation (`Your current data will be replaced with the selected backup. Do you want to continue?`).
  - Controls: Close (`✕`) control at top-right. Actions: `Go Back` (returns to first dialog) and `Restore` (starts restoration).
- **Progress & Asynchronous Flow:**
  - Blocking progress overlay showing `Restoring your data…` with a `CircularProgressIndicator`.
  - Restores database atomically inside an SQLite transaction and updates DataStore preferences asynchronously.
  - Prevents duplicate restore attempts during execution.
  - Shows success dialog (`✓ Restore Complete`) only after successful completion and refreshes ViewModel state/navigates smoothly.
  - On error, shows error dialog (`Invalid Backup File` / failure message) and preserves existing local data.

---

## Issue 2: Calendar-Aware Monthly Reminders

### Implementation Details
- **Date Calculation (`MonthlyReminderCalculator`):**
  - Extracted month-end date calculation into `MonthlyReminderCalculator`.
  - Computes the exact last day of the calendar month for 28, 29, 30, and 31-day months, properly handling leap years (e.g. Feb 29 on leap years, Feb 28 on non-leap years).
  - Respects the user-selected reminder time (`targetHour` and `targetMinute`).
  - Automatically rolls over to the last day of the next calendar month if the target time for the current month has passed.
- **WorkManager Scheduling (`NotificationScheduler`):**
  - Replaced the 30-day periodic schedule (`PeriodicWorkRequestBuilder`) with a precise `OneTimeWorkRequestBuilder<MonthlyReviewWorker>` targeting the exact last calendar day of each month.
  - Cancels any existing periodic or one-time monthly review tasks before enqueuing the new schedule (`ExistingWorkPolicy.REPLACE`).
  - Reschedules automatically after a monthly review fires (`MonthlyReviewWorker.doWork()`) and across device restarts via `BootReceiver` reading saved DataStore preferences.

---

## Issue 3: Complete Offline Backup & Safe Restoration

### Implementation Details
- **Complete Data Preservation (`BackupSnapshot`):**
  - Audited all user-owned data and updated `BackupSnapshot` to store:
    - Profile name and portable preferences: `themeMode`, `trackingMode`, `notificationsEnabled`, `dailyReminderEnabled`, `weeklyReviewEnabled`, `monthlyReviewEnabled`, `planEndingEnabled`, `reminderHour`, `reminderMinute`.
    - Transactions and transaction details.
    - Default and custom categories.
    - Financial plans (both active and past/completed plans).
    - Planned income and planned expense entries.
  - Device-specific OS permissions and local runtime notification state markers (e.g. milestone flags) are kept local and excluded from portable exports.
- **Validation & Safe Deserialization (`BackupManager`):**
  - Validates app identifier (`TermRunway`), schema version (`1` and `2`), and non-negative transaction/planned amounts.
  - Validates parent-child relationships: verifies that all `plannedIncomes` and `plannedExpenses` reference a valid plan present in `plans`.
  - Provides fallback default values for portable preference fields when reading legacy Version 1 backups, maintaining full backwards compatibility.
- **Atomic Restoration (`TermRunwayRepository`):**
  - Executes database replacement inside a single SQLite transaction (`db.beginTransaction()`). If any failure occurs, the transaction rolls back cleanly without leaving partially replaced data.
  - Remaps regenerated database IDs for plans while preserving parent-child relationships for planned entries.
  - Re-syncs notifications (`NotificationScheduler.syncFromPreferences`) and refreshes repository/ViewModel caches immediately on success.

---

## Issue 4: Date, Time & Time-Zone Architecture Corrections

### Implementation Details
- **Date Semantics & Type System:**
  - Calendar dates (transaction date, plan start/end dates, planned item dates) are stored as ISO-8601 strings (`YYYY-MM-DD`) using `java.time.LocalDate`.
  - Instants (`createdAtMs`) remain epoch milliseconds representing precise real moments in time.
  - Disentangled time zone changes from calendar dates: `"2026-10-18"` remains October 18, 2026 regardless of device time zone shifts.
- **Database Schema Migration (v2 → v3):**
  - Incremented SQLite schema version to 3 in `TermRunwayDatabase.kt`.
  - Migrated `transactions`, `plans`, `planned_income`, and `planned_expense` tables to ISO-8601 string columns (`date_str`, `start_date_str`, `end_date_str`, `expected_date_str`).
  - Added `migrateToVersion3()` that reads legacy `_ms` columns and converts them to `YYYY-MM-DD` strings via cursor loop, preserving all record IDs, amounts, categories, and foreign key constraints without data loss.
- **Date Calculations & Day Math:**
  - Removed fixed `86_400_000L` day duration constants from `FinancialCalculator` and `UiFormatting`.
  - Replaced millisecond day math with `ChronoUnit.DAYS.between(start, end) + 1` for inclusive day calculations, correctly supporting leap years, month boundaries, and daylight-saving transitions.
- **Backup Format Schema Version 2:**
  - Updated `BackupManager` to export backup format schema version 2 using ISO date strings for calendar dates.
  - Retained full support for importing legacy schema version 1 backups with `dateMs` / `startMs` / `endMs` epoch millis.
- **System Event Listeners & Responsive UI:**
  - Configured `BootReceiver` and `AndroidManifest.xml` to handle `ACTION_BOOT_COMPLETED`, `ACTION_MY_PACKAGE_REPLACED`, `ACTION_TIMEZONE_CHANGED`, and `ACTION_TIME_SET` (`ACTION_TIME_CHANGED`) to reschedule notifications immediately on system clock/timezone changes.
  - Configured `MainActivity` to register a dynamic `BroadcastReceiver` for time ticks, date changes, and timezone changes, invoking `viewModel.refresh()` on `onResume()` and on system clock ticks across midnight.

---

## Verification Results

### Automated Unit Tests
Executed `./gradlew test` with **63 tests passing and 0 failures**:
- `fullDatasetRoundTripTest` & `schemaVersion2RoundTripTest`: Verified full dataset export and restore for both Schema Version 1 and Schema Version 2.
- `legacyVersion1MigrationTest`: Verified importing legacy schema version 1 backups into `LocalDate` domain models.
- `migrationFromVersion2ToVersion3PreservesDataAndConvertsDatesToIsoStrings`: Verified database schema upgrade from v2 to v3, converting legacy epoch millis to `YYYY-MM-DD` strings while preserving all record IDs and amounts.
- `iso8601StringParsingPreservesCalendarDateIndependentlyOfTimeZone`: Verified time-zone invariance of `LocalDate` strings.
- `leapYearDaysInclusiveCalculation`: Verified 29 days in Feb 2028 and 366 days in leap year 2028 vs 365 days in 2027.
- `monthBoundaryInclusiveDaysCalculation`: Verified inclusive day count across Jan 31 -> Feb 1 month boundary.
- `planMetricsCalculateRemainingMoneyAndDailyGuidance`: Verified remaining runway and safe spending calculations with `LocalDate`.
- `monthlyReminderTargetFor28DayFebruaryNonLeapYear` & `monthlyReminderTargetFor29DayFebruaryLeapYear`: Verified month-end reminder calculations.

### Build Verification
- Executed `./gradlew assembleDebug`: Build finished successfully with 0 errors.

---

## Status Summary
- Issue 1 (Restore feedback and navigation order): **VERIFIED & COMPLETED**
- Issue 2 (Calendar-aware monthly reminders): **VERIFIED & COMPLETED**
- Issue 3 (Complete offline backup and safe restoration): **VERIFIED & COMPLETED**
- Issue 4 (Date, time and time-zone corrections): **VERIFIED & COMPLETED**
