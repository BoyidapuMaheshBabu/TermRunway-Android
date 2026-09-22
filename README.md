# TermRunway Android

> **A student-focused personal finance app for tracking real spending and planning money across a semester.**

TermRunway is a student-focused personal finance app designed to help students understand and manage their money during a semester.

This repository contains the **Android version of TermRunway**, where the original TermRunway idea is being rebuilt as a native mobile application.

## Why TermRunway moved from Web to Android

TermRunway originally started as a web application. The web version helped turn the idea into a working product and taught me a lot about the problem, the user flow, and the financial calculations.

But while developing the web version, I realized that TermRunway was becoming more than a website with a dashboard. The main idea is about something students may use regularly to record transactions, plan their semester, and check their financial situation.

Because of that, I decided to stop treating the web version as the final destination and move the project toward a **native Android application**.

The web version was an important step, not a failure or wasted work. It helped me validate the concept, understand the workflow, identify what the product actually needs, and rethink how TermRunway should work as a complete application.

Now I am building the Android version from the idea itself rather than simply converting every web screen into a mobile screen.

## Why build the Android app?

The Android version gives me the opportunity to focus on the actual TermRunway system:

- How financial inputs should work
- How the calculations should behave
- How Tracking Mode and Plan Mode should differ
- How results should be presented to the student
- How the app should respond when a plan becomes financially difficult
- How the product can become useful as an everyday student finance tool

The goal is to build the app around the **problem and the logic**, not just around the UI.

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

The main purpose is simple:

> Record what actually happened and understand it.

## Plan Mode

Plan Mode is designed for students who want to plan their money for a specific period, such as a semester.

The planning input can include:

- From date and to date
- Available money now
- Expected income
- Expected expenses
- Expense categories

For example, a student may have some money available now, expect additional support from parents or part-time work, and already know several expenses that may occur during the semester.

The app can use these inputs to calculate the financial outlook for the planning period.

A basic projection is:

```text
Projected Balance
= Available Money
+ Expected Income
- Planned Expenses
```

The planning process should not simply wait until the end to show a result. As the student changes the inputs, TermRunway should calculate the effect and provide useful indications.

For example, when planned expenses become too high compared with available money and expected income, the app can indicate that the current plan may not be sufficient and that the student should review expenses or expected income.

This is one of the areas where TermRunway's financial calculations become an important part of the product.

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

The same visualization idea can be used with actual transaction data in Tracking Mode and planned values in Plan Mode.

## Android Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android SDK
- Gradle

## Project Direction

TermRunway is being developed as a practical software project.

My development process is:

```text
Understand the problem
        ↓
Define the rules and calculations
        ↓
Design the workflow
        ↓
Implement
        ↓
Test
        ↓
Debug
        ↓
Improve
```

The Android application is the next stage of the TermRunway idea. Instead of simply continuing the web implementation, I am using what I learned from the web version to rebuild the product around the original problem and the way I want TermRunway to work.

## Related Project

Web version: [TermRunway](https://github.com/BoyidapuMaheshBabu/TermRunway)

---

**TermRunway — understand your money, plan your runway.**
