# Future V2 Candidate — Saving Goals 🎯

## Status

**Proposed for V2 — phase number not assigned yet**

## V2 Planning Objective

Add a focused Saving Goals capability that lets a student define a target amount, record intentional contributions, and understand what remains to reach the goal.

The feature must extend TermRunway's existing planning model without creating a separate financial system.

## Product Requirements

### Goal Creation

A student can create a goal with:

- name
- target amount
- optional target date

Creation must validate that:

- the name is not empty
- the target amount is greater than zero
- the target date, when provided, is valid

### Goal Progress

Each goal shows:

- target amount
- amount saved
- remaining amount
- optional target date
- status

The remaining amount should never be shown as a negative requirement. When savings reach or exceed the target, the goal is completed.

### Contributions

A student can add an explicit contribution to a goal.

A contribution:

- increases the goal's saved amount
- does not become an expense
- does not become income
- is stored locally
- has a date
- may optionally contain a short note

The product must make this semantic distinction clear.

### Time Guidance

When a target date exists, calculate the amount needed per remaining day.

When no target date exists, do not invent a deadline or pace.

Guidance should be contextual and non-judgmental.

### Navigation

Do not add a new bottom-navigation destination.

Use the established information architecture:

**Home | Plan | + | Activity | Insights**

Recommended placement:

- Plan → primary goal management
- + → create goal / add contribution
- Home → compact active-goal summary
- Insights → goal context only when it adds real value

## Acceptance Criteria

- [ ] Goal creation works fully offline.
- [ ] Goal records remain local.
- [ ] Target amounts use the existing integer-paise money representation.
- [ ] A goal can exist without a target date.
- [ ] Contributions are distinct from income/expense transactions.
- [ ] Saved and remaining amounts update immediately after a contribution.
- [ ] Completed goals are detected reliably.
- [ ] A target date produces understandable money-per-day guidance.
- [ ] Missing/invalid inputs are handled clearly.
- [ ] Editing and deleting/archiving behavior is deterministic.
- [ ] Backup/restore preserves goals and contributions.
- [ ] Existing Daily Tracking behavior is unchanged.
- [ ] Existing Plan Tracking behavior remains coherent.
- [ ] Existing navigation is preserved.
- [ ] The feature works without network access.
- [ ] The feature is tested on a physical Android device.

## Non-Goals

This phase does not include:

- bank synchronization
- automatic money transfers
- cloud accounts
- shared goals
- investment tracking
- interest calculations
- complex recurring contribution rules

## Review Questions

Before closing the phase:

1. Does the feature solve a real student problem?
2. Is the distinction between spending and saving allocations understandable?
3. Does the feature remain consistent with the two-mode product model?
4. Does it preserve the money-first language of TermRunway?
5. Is the implementation still simple enough to maintain locally?
6. Does backup/restore preserve the complete goal state?


## Versioning Note

This document is **not V1 Phase 16**. V1 ends at Phase 15. If Saving Goals is selected for V2, it will receive a new V2 phase number beginning from the V2 Phase 01 sequence.
