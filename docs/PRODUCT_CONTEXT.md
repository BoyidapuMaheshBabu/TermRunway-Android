# TermRunway Product Context

## Product Identity

**TermRunway** is a student-focused personal finance and semester planning application.

Its central idea is not simply to record spending. It is to help a student understand:

> **Where did my money go, and how far can my remaining money take me?**

The name "TermRunway" connects money management with a student's academic term: the available money is the runway, and the semester is the period that runway must support.

## Current Product

The active product is **TermRunway Android 1.0.0**.

Implementation repository:

[github.com/BoyidapuMaheshBabu/TermRunway-Android](https://github.com/BoyidapuMaheshBabu/TermRunway-Android)

The previous web implementation is discontinued and preserved as product history.

## Core Modes

### Daily Tracking — "What happened?"

Daily Tracking is for understanding actual money movement.

Typical needs:

- record income
- record expenses
- categorize transactions
- review recent activity
- understand daily/weekly/monthly spending
- see useful trends and totals
- know how much money has actually been spent

It does not require the student to create a financial plan.

### Plan Tracking — "What will happen?"

Plan Tracking is the core planning experience for a defined period such as a semester.

The student can define:

- period start date
- period end date
- expected income
- expected expenses
- categories such as food, transport, study, bills, entertainment, and other needs

The product should then help answer practical questions such as:

- How much money is available?
- How much is planned to be spent?
- How much remains?
- How much time remains?
- What does the remaining money mean for the remaining period?
- How is actual spending comparing with the plan?

Planning should use real transaction data rather than becoming a separate disconnected budgeting system.

## Target User

The primary target is a student who manages a limited amount of money during college.

The product is intentionally not designed first for:

- professional accounting
- investment management
- bank aggregation
- business bookkeeping
- complex financial forecasting

## Core Problem

Students can know individual expenses without understanding their overall financial runway.

TermRunway connects:

**money + time + actual spending + planned spending**

into a student-oriented view.

## Product Principles

1. **Offline first** — core functionality should work without internet.
2. **Privacy by default** — financial information should remain on the device.
3. **Numbers before decoration** — useful monetary values are more important than percentage-heavy dashboards.
4. **Planning should connect to reality** — planned and actual transactions should work together.
5. **Guidance should be contextual** — spending feedback should account for time, remaining money, and the plan rather than judging one transaction in isolation.
6. **Simple does not mean shallow** — the interface should remain understandable while the underlying model remains sound.
7. **Function before polish** — correctness comes before visual refinement.
8. **Real-device validation** — behavior should be proven on physical devices, not only assumed from development builds.
9. **No invented user data** — the product must never fabricate financial history, income, or experience.
10. **AI assists; the developer owns the decision**.

## Technical Direction

The Android product is intentionally:

- native Android
- Kotlin-based
- Jetpack Compose + Material 3
- offline-first
- local-data oriented
- free from a required backend
- designed without mandatory accounts
- designed without mandatory internet access

Financial amounts are represented using integer paise internally to avoid floating-point money errors.

## Product Evolution

```
Initial Idea
   ↓
Web Prototype
   ↓
Build + Realize Limitations
   ↓
Product Reconsideration
   ↓
Native Offline Android Direction
   ↓
Architecture Foundation
   ↓
Feature / UX Refinement
   ↓
Plan + Semester Experience
   ↓
Data Safety + Settings
   ↓
Performance + Testing
   ↓
V1 Finalization
```

The web prototype was valuable because it exposed product and architecture limitations. Moving away from it was a product decision, not a failure.

## Product Ownership

The product owner is responsible for:

- identifying the problem
- deciding what matters
- evaluating trade-offs
- defining acceptance criteria
- reviewing implementation
- testing behavior
- maintaining product direction

AI tools are part of the workflow, but the product's decisions remain human-owned.
