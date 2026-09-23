# TermRunway Android 💸

> **A student-focused Android app for understanding everyday money through simple, local-first tracking.**

TermRunway Android is being built as an offline-first student finance app. Version 1 focuses completely on the **Daily Tracker**. Term planning and term-flow management are intentionally kept out of the v1 navigation until the Daily Tracker is complete.

## Daily Tracker

Daily Tracker answers:

> **What happened?**

Students can:

- record expenses
- record income
- choose an expense category or income source
- attach an optional note
- choose today or an earlier date
- review a day at a time
- edit or delete transactions
- set an optional daily spending limit
- receive small informational spending indications without blocking transactions

The app uses the same underlying transaction data for Home, Track, and Insights.

## Screens

| Screen | Purpose |
| --- | --- |
| **Home** | Quick view of today's spending, income, recent activity, and the next useful action. |
| **Track** | Detailed day-by-day transaction recording and review. |
| **Insights** | Understand spending patterns and category totals over selected periods. |
| **Settings** | Profile, daily limit, theme, backup/restore, and data deletion. |

## Backup & Restore

TermRunway can export a complete restoreable JSON backup containing local profile information, expenses, income, and relevant preferences.

Backups use a filename such as:

```text
TermRunway_Mahesh_2026-09-23.json
```

Calculated totals and charts are not stored as the source of truth. They are rebuilt from restored transaction data.

## Product principles

- Money values are primary; percentages are not part of the core planning experience.
- A high-spending day is not automatically treated as a failure.
- Daily limits are informational and never block a real transaction.
- Empty states and insufficient-data states are explicit rather than showing fake statistics.
- User data remains local unless the user deliberately creates or restores a backup.

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- Android SDK
- AndroidX
- Gradle
- Git and GitHub

## Development approach

```text
Understand
   ↓
Design
   ↓
Build
   ↓
Test
   ↓
Fix
   ↓
Improve
```

Term Mode will be implemented separately after the Daily Tracker has been completed and validated.

## Developer

**Boyidapu Mahesh Babu**

GitHub: [@BoyidapuMaheshBabu](https://github.com/BoyidapuMaheshBabu)

