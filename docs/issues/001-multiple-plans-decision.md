# Issue 001 — Should TermRunway Support Multiple Plans?

## Status

**Open — Decision not made**

> This issue is intentionally unresolved. No accepted product decision has been taken yet.

## Product Question

Should TermRunway support **multiple financial plans**, or should the product continue with **one plan at a time**?

This is a product-level decision, not an implementation task.

## Why This Is Unresolved

The current product has an **Active Plan** concept and the V1 experience is being refined around a single current plan.

However, real users may eventually have different planning needs, such as:

- a college semester plan
- a short-term event or trip plan
- another future financial period

Before designing or implementing multiple-plan behavior, TermRunway needs a clear product decision about whether this belongs in the product and, if so, what the model should be.

## Decision That Needs to Be Made

The final decision should answer:

1. **Can a user have more than one saved plan?**
2. **Can more than one plan be active at the same time?**
3. **How should plans relate to Activity and Insights?**
4. **How should the user choose or switch between plans?**
5. **What happens to completed and future plans?**
6. **Does multiple-plan support belong in V1, or should it be deferred to V2?**

## Directions Under Consideration

### Direction A — One Plan at a Time

TermRunway keeps a single plan model.

- one current/active plan
- no plan browser
- simpler experience
- easier mental model

### Direction B — Multiple Saved Plans, One Active Plan

A user can keep multiple plans, but only one can be the Active Plan at a time.

- future and completed plans can exist
- Activity and Insights use the selected/active context where appropriate
- avoids overlapping active plans

### Direction C — Multiple Concurrent Active Plans

A user can have multiple plans active at the same time.

- more flexible
- much more complex
- requires clear rules for transaction attribution, Activity, Insights, remaining money, and overlapping periods

This direction should not be assumed without a deliberate product decision.

## Current Product Position

**No direction has been accepted yet.**

The existing Active Plan behavior should continue to work without treating this issue as decided.

Do not add multiple-plan implementation to a phase until this issue reaches **Decision Ready** and an accepted decision is recorded in the decisions folder.

## Resolution Path

When the product decision is made:

**Open → Discussion / Research → Decision Ready → Decided**

After acceptance:

1. Create the corresponding accepted decision in the decisions folder.
2. Update this issue status to **Decided**.
3. Only then define implementation scope in the appropriate phase.

## Important Boundary

This issue does **not** mean that multiple-plan support is approved.

It exists so the question is visible, preserved, and explicitly resolved before implementation.
