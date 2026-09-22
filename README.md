# TermRunway Android

TermRunway is a student-focused personal finance app designed to help students understand and manage their money during a semester.

This repository contains the **Android version of TermRunway**.

## Core Idea

TermRunway is built around two different ways of working with student finances:

| Mode | Main question | Purpose |
| --- | --- | --- |
| **Tracking Mode** | What happened? | Record actual income and expenses and understand past spending. |
| **Plan Mode** | What will happen? | Plan a semester budget using available money, expected income, and expected expenses. |

Both modes lead to **Insights**, but the calculations and meaning of those insights are different.

## Tracking Mode

Tracking Mode is for students who do not want to create a fixed budget first.

The student records real transactions such as:

- Income
- Food expenses
- Transport expenses
- Accommodation expenses
- Other spending

TermRunway can then use those transactions to show historical financial information such as spending over a selected period, income versus expenses, category spending, and spending patterns.

## Plan Mode

Plan Mode is designed for students who want to plan their money for a specific period, such as a semester.

The planning input can include:

- From date and to date
- Available money now
- Expected income
- Expected expenses
- Expense categories

The app can use these inputs to calculate the financial outlook for the planning period.

A basic projection is:

```text
Projected Balance
= Available Money
+ Expected Income
- Planned Expenses
```

During planning, the goal is to give useful indications while the student changes the numbers.

For example, when planned expenses become too high compared with available money and expected income, TermRunway can indicate that the current plan may not be sufficient and that the student should review expenses or expected income.

## Insights

The Insights layer combines the results produced by the two modes.

### Tracking Mode
- Actual income and expenses
- Historical spending analysis
- Period-based spending views
- Income vs. expense trends

### Plan Mode
- Forecast
- Projected balance
- Planning-period timeline
- Expected income and expenses
- Plan status and warnings

## Spending & Income

One of the main visualizations is an income-versus-expense view over time.

```text
₹
│
│          ● Income
│      ●
│
│  █ Expense       █
│       █     █
│  █    █     █
└────────────────────────
  1    5    10   15   20
             Days
```

The same idea can be used with actual transaction data in Tracking Mode and planned values in Plan Mode.

## Android Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android SDK
- Gradle

## Project Direction

TermRunway is being developed as a practical software project, with the focus on understanding the problem, defining the calculations, implementing them, testing them, and improving the product step by step.

The Android application is a continuation of the TermRunway idea from the web project into a native mobile application.

## Related Project

Web version: [TermRunway](https://github.com/BoyidapuMaheshBabu/TermRunway)

---

**TermRunway — understand your money, plan your runway.**
