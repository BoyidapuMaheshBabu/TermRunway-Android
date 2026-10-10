# Decision 006: Saving Goals

## Context

TermRunway already connects actual financial activity with future planning, but a semester plan does not directly represent a student's separate savings target.

A student may want to save toward a specific amount while still managing ordinary semester spending.

## Options Considered

### 1. Separate savings app / mode

Rejected.

This would fragment the product and weaken the established two-mode model.

### 2. Treat savings as an expense

Rejected.

A saving allocation is not consumption. Counting it as an expense would distort spending totals and create double-counting risk.

### 3. Derive savings only from the current balance

Rejected.

A balance does not reveal how much money a student intentionally considers committed to a specific goal.

### 4. Add explicit goal contributions as a planning layer

Chosen.

A goal tracks a target and explicit local contributions. Contributions are separate from income and expense transactions.

## Decision

TermRunway will support **Saving Goals** as a focused planning layer.

A goal may contain:

- name
- target amount
- saved/contributed amount
- optional target date
- status

The feature will not create a new top-level navigation mode.

Primary placement is within **Plan Tracking**, with quick goal actions available from the existing central  action.

## Why

This approach:

- preserves the existing Daily Tracking + Plan Tracking product model
- keeps spending totals semantically correct
- makes savings progress explicit instead of inferred
- supports concrete money-first guidance
- works offline with local data
- allows time-based saving guidance when a target date exists
- avoids unnecessary banking or cloud infrastructure

## Consequences

### Easier

- students can track a concrete savings objective
- goal progress can be explained in money values
- future savings can coexist with semester planning
- the feature remains understandable within the existing navigation

### Harder

- the data model needs a goal/contribution concept in addition to income and expense transactions
- backup/restore must preserve goal records and contributions
- editing, deletion, archiving, and completed-goal behavior must be defined carefully
- actual cash movement and virtual earmarking must remain clearly distinguished

## Scope Boundary

The first implementation should not include:

- bank integration
- automatic transfers
- cloud synchronization
- investments or interest
- shared goals
- complex recurring automation

## Status

**Accepted — planned as the next focused product feature after V1.0 stabilization.**

## Related

- Phase: [Phase 16 — Saving Goals](../phases/16-saving-goals.md)
- Product Rules: [Product Rules](../PRODUCT_RULES.md)
