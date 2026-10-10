# TermRunway Product Rules

These rules protect the product's identity while implementation evolves.

## 1. Offline Means Core Offline

Core TermRunway functionality must remain usable without internet access.

Do not introduce a required network dependency for ordinary tracking or planning.

## 2. Financial Data Stays Local

The product should not require uploading personal financial information to a server.

Any future cloud feature would require an explicit product decision and privacy review.

## 3. Money Is the Primary Language

Show useful monetary values first.

Do not force users to interpret percentages when a direct amount answers the question better.

Example:

**₹2,400 remaining** is usually more immediately useful than **32% remaining**.

## 4. Planning Is About Time + Money

A plan is not only a budget number.

The product should consider:

- remaining money
- remaining days
- actual spending
- planned spending

when providing guidance.

## 5. Actual and Planned Data Must Connect

Plan Tracking must not become an isolated second budgeting application.

The same underlying transaction reality should support both modes.

## 6. Context Before Judgment

A single high-spend transaction does not automatically mean the user is doing badly.

Guidance should consider the relevant period, remaining money, plan, and time.

## 7. Do Not Over-Engineer V1

A technically sophisticated feature is not automatically a useful feature.

Prefer a smaller correct system over a larger fragile one.

## 8. Functional Correctness Comes First

The development priority is:

**Correct → Reliable → Understandable → Consistent → Beautiful**

Visual polish should not hide broken behavior.

## 9. Real Devices Matter

The product must be tested on physical Android devices before treating important behavior as verified.

## 10. Privacy Is a Product Feature

Privacy is not merely an implementation detail.

The absence of unnecessary accounts, backend services, and data collection is part of the product value.

## 11. AI Does Not Own Product Decisions

AI can propose, explain, implement, refactor, test, and research.

The product owner decides what is accepted.

## 12. No Fake Completeness

Documentation must distinguish:

- implemented
- planned
- experimental
- proposed
- discontinued

Do not describe future functionality as if it already exists.

## 13. Documentation Should Be Durable

Document decisions that affect future work.

Do not turn this repository into a transcript archive.

## 14. Phase Completion Is Developer-Confirmed

A phase is not officially complete because implementation, builds, tests, or review appear successful.

**Only the developer/product owner can declare a phase complete.**

AI must treat an explicit statement such as:

- "Phase 16 is completed"
- "I accept Phase 16"
- "This phase is complete"

as the authoritative completion signal.

Before that explicit confirmation, AI must not mark the phase as completed/established in the documentation in `docs/`, even if the implementation appears finished.

When the developer explicitly confirms phase completion, AI should update the relevant phase status and any directly affected canonical product-status documents so that they reflect the confirmed state.

This preserves human ownership of acceptance while allowing AI tools to keep product documentation synchronized.

## 15. Major-Version Phase Numbering

Phase numbering is scoped to the major product version.

- The current development cycle is **V1 Phase 01–15**.
- **V1 Phase 15 — V1 Finalization** is the final V1 phase.
- Do not create **V1 Phase 16**.
- After explicit V1 completion/acceptance, the next major development cycle begins at **V2 Phase 01**.

> **Phase numbers restart with a new major product version.**

## 16. Phase and Branch Discipline

Product phases and Android branches are related, but they are not created in bulk.

- The documentation in `docs/` records the planned phases and their exact canonical Android branch names.
- **V1 branches use `v1/phase/<phase-number>-<phase-name>`. V2 will use `v2/phase/<phase-number>-<phase-name>`.**
- The Android repository creates **only the next active phase branch** when development begins.
- Every new phase branch starts from the latest Android `main`.
- The active phase should not absorb unrelated future-phase work.
- After implementation, validation, and explicit developer acceptance, the phase branch is merged into `main`.
- Completed phase branches remain frozen as historical checkpoints.
- Do not merge later `main` changes back into old phase branches.
- The next phase branch is created only after the previous phase has been accepted and merged.

> **One phase. One active branch. One controlled merge.**

## 17. Presentation Is Not the Product

Features should exist because they solve user problems, not merely because they look impressive in a college presentation.
