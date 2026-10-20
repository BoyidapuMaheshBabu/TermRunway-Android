# Issue 002 — Saving Goals as a V2 Candidate 🎯

## Status

**Open — Proposed V2 candidate. No phase number assigned.**

- GitHub tracking issue: [#27 — Saving Goals](https://github.com/BoyidapuMaheshBabu/TermRunway-Android/issues/27)
- Decision status: proposed, not accepted for implementation
- Version boundary: V2 candidate only; **not V1 Phase 16**

This record preserves the proposal for future V2 planning. It is not an accepted product decision or implementation-ready phase. V1 ends at Phase 15. If selected, Saving Goals will receive a phase number in the V2 sequence beginning at V2 Phase 01.

## Product Objective

Let a student define a savings target, record intentional contributions, and understand how much remains to reach the target. The feature must extend TermRunway's existing planning model rather than introduce a separate financial system.

## Proposed Product Scope

### Goal creation

A student can create a goal with:
- A non-empty name
- A target amount greater than zero
- An optional target date

The app must reject an empty name, a target amount less than or equal to zero, and an invalid date when one is supplied. A goal without a target date is valid.

### Goal progress

Each goal should show:
- Target amount
- Amount saved
- Remaining amount
- Optional target date
- Status

Remaining amount must never be negative. When saved money reaches or exceeds the target, remaining amount is zero and the goal is completed.

### Explicit contributions

A student can add a contribution to a goal. Each contribution:
- Increases the goal's saved amount
- Is **not an expense and not income**
- Is stored locally
- Has a date
- May include a short optional note

The UI and product language must make clear that a contribution is a savings allocation, not a transaction category for spending or earning.

### Time guidance

When a goal has a target date, show understandable money-per-remaining-day guidance. When there is no target date, do not invent a deadline or pace. Guidance must be contextual and non-judgmental.

V2 planning must settle date-boundary, past-date, and rounding behavior before implementation.

### Navigation

Preserve the existing bottom navigation:

**Home | Plan | + | Activity | Insights**

- **Plan:** primary goal management
- **+:** create a goal or add a contribution
- **Home:** compact summary of active goals
- **Insights:** show goal context only when it adds real value

Do not add a new bottom-navigation destination.

## Acceptance Criteria

- [ ] Goal creation works fully offline.
- [ ] Goal records remain local.
- [ ] Target amounts use the existing integer-paise money representation.
- [ ] Goals may omit a target date.
- [ ] Contributions remain separate from income and expense transactions.
- [ ] Saved and remaining amounts update immediately after a contribution.
- [ ] Goals become completed when saved amount reaches or exceeds target amount.
- [ ] Remaining amount never displays as a negative requirement.
- [ ] A target date produces understandable money-per-day guidance; no target date means no invented pace.
- [ ] Missing and invalid inputs are handled clearly.
- [ ] Editing and deleting/archiving behavior is deterministic.
- [ ] Backup/restore preserves goals and contributions, including dates and notes.
- [ ] Existing Daily Tracking behavior is unchanged.
- [ ] Existing Plan Tracking behavior remains coherent.
- [ ] Existing navigation is preserved.
- [ ] The feature works without network access.
- [ ] The implementation is tested on a physical Android device.

## Non-Goals

- Bank synchronization
- Automatic money transfers
- Cloud accounts
- Shared goals
- Investment tracking
- Interest calculations
- Complex recurring contribution rules

## Review Questions Before V2 Selection

1. Does this solve a real student problem?
2. Is the distinction between spending and savings allocations understandable?
3. Does the feature fit TermRunway's two-mode product model?
4. Does it preserve the product's money-first language?
5. Is the implementation simple enough to maintain locally?
6. Does backup/restore preserve the complete goal state?

## Resolution Path

**Open → Discussion / Research → Decision Ready → Accepted Decision or Rejected / Deferred**

If accepted:
1. Record the final product decision in `docs/decisions/`.
2. Finalize unresolved behavior, including contribution history and goal deletion/archive semantics.
3. Assign an explicit V2 phase number and create its phase documentation.
4. Create the V2 development branch only when that phase begins.

Until then, existing V1 scope and behavior remain unchanged.
