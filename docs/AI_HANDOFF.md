# AI Handoff 🤖

> **Durable context recovery for new TermRunway AI sessions.**

TermRunway is developed with multiple AI tools and development environments. This document defines how a new AI session should regain product context without depending on old conversations.

## 🎯 Purpose

A new AI session should understand the product before proposing or implementing changes.

The goal is not to preserve conversation history. The goal is to preserve the **decisions, constraints, current state, and reasoning that matter**.

> **AI context may change. Product knowledge should not.**

## 📚 1. Read the project documentation first

Before implementation, read the relevant documents in this repository.

Recommended order:

1. [PRODUCT_CONTEXT.md](PRODUCT_CONTEXT.md) — product identity, problem, users, scope, principles
2. [CURRENT_STATE.md](CURRENT_STATE.md) — current implementation/product status
3. [PRODUCT_RULES.md](PRODUCT_RULES.md) — non-negotiable rules
4. [ROADMAP.md](ROADMAP.md) — current direction and future scope
5. [DEVELOPMENT_APPROACH.md](DEVELOPMENT_APPROACH.md) — engineering workflow
6. [INDEX.md](INDEX.md) — repository navigation
7. Relevant [decisions](decisions/README.md)
8. Relevant [phase](phases/README.md) documentation
9. Review the relevant product context, decisions, and phase documentation before making a new product decision

Do not read every historical document by default. Read what is relevant to the requested task.

## 📱 2. Analyze the Android Repository

After understanding product context, inspect **TermRunway-Android** before proposing implementation changes.

The Android repository is the implementation source of truth.

Check the current:

- architecture
- navigation
- data model
- relevant screens
- repository/domain logic
- tests
- build configuration
- existing implementation of the requested feature

Do not assume that the documentation in `docs/` describes every current implementation detail.

## 🧠 3. Report Understanding Before Implementation

For a meaningful change, the AI should first establish:

- What problem is being solved
- Which user workflow is affected
- Which existing product decision supports the change
- What is already implemented
- What is missing
- Relevant constraints
- Likely files/components affected
- What should **not** be changed

If there is a conflict between documentation and implementation, surface it instead of silently choosing one.

## 🎯 4. Define the Task

Convert the request into a focused implementation scope.

A good task definition includes:

**Problem → Goal → Requirements → Constraints → Acceptance Criteria**

Avoid expanding the task with unrelated refactoring, redesign, or infrastructure.

## 🛠️ 5. Implement in a Focused Phase

Implementation should follow the project's normal engineering trail:

**Decision → Phase → Current Branch → Implementation → Commit → Test → Review → Merge**

For implementation work:

- use the exact Android branch name recorded for the active phase
- create only the active phase branch when that phase begins
- start the branch from the latest Android `main`
- do not pre-create future phase branches

- inspect before changing
- preserve existing architecture unless a documented decision requires otherwise
- change only what the task requires
- avoid unnecessary rewrites
- preserve offline-first behavior
- preserve financial correctness
- avoid introducing backend/network requirements without an explicit product decision
- use the Android repository for code changes

AI-generated code is not automatically accepted as correct.

## 🧪 6. Validate

Validation should match the change.

Where applicable:

- build/compile
- unit or functional tests
- financial calculation checks
- edge cases
- physical-device testing
- UX behavior
- regression checks

A feature is not complete merely because generated code looks correct.

## 🔍 7. Review

After implementation, review two dimensions:

### Engineering
- Does it compile?
- Does it behave correctly?
- Does it preserve existing architecture?
- Are edge cases handled?
- Are tests appropriate?

### Product
- Does it solve the intended problem?
- Is the UX understandable?
- Does it follow product rules?
- Does it introduce unnecessary complexity?
- Does it create a new durable decision?

## 📝 8. Preserve New Knowledge

Update the documentation in `docs/` only when the work creates durable knowledge.

Examples:

- a new product decision
- a changed product rule
- a meaningful architecture direction
- a new phase requirement
- a research conclusion
- a changed product scope
- a change to the AI workflow

Do not copy temporary prompts, full conversations, generated code, or routine implementation noise into this repository.

## ⚠️ 9. Never Invent Product State

Clearly distinguish:

- **Implemented**
- **Planned**
- **Proposed**
- **Experimental**
- **Discontinued**

Do not claim a feature exists because it was discussed.

Use the Android repository and current product documentation to verify implementation status.

## 🔢 Major-Version Phase Numbering

Before working on a phase, identify its major product version.

The current sequence is **V1 Phase 01–15**. V1 ends at V1 Phase 15.

After V1 is explicitly completed and accepted, the next development cycle restarts at **V2 Phase 01**. AI must not invent a V1 Phase 16.

When V2 begins, use the V2 phase identity recorded by the documentation in `docs/` and do not treat V2 Phase 01 as a continuation of V1 history.

## 🗂️ Phase Status Interpretation

TermRunway phases have **two separate status dimensions**. AI sessions must not collapse them into one status.

### 1. Phase / Branch Status

Describes whether the phase has been **established/prepared as a development unit**.

Examples:

- **✅ Completed** — the phase setup/history is established and the phase has been accepted as complete.
- **✅ Phase prepared** — the Product phase is defined and documented; its Android implementation branch may not exist yet.
- **⏳ Proposed / not prepared** — the phase is only proposed and has not yet been prepared as a Product development unit.

### 2. Feature / Refinement Status

Describes whether the **actual work defined by that phase** has been implemented, validated, and accepted.

Examples:

- **✅ Completed**
- **⏳ Implementation pending**
- **⏳ Refinement pending**
- **⏳ Hardening pending**
- **⏳ Validation pending**
- **🚧 Current work pending**

A phase may improve, harden, optimize, polish, or handle edge cases in existing functionality without introducing a new feature. Such work is still valid phase work.

A prepared Product phase does **not** mean its Android branch already exists or that its implementation is complete.

Phases may be documented ahead of implementation. Android phase branches are created **one at a time**, only when that phase begins.

AI must inspect the actual implementation and relevant history before determining Feature / Refinement Status. Do not infer completion from:

- branch existence
- phase-file existence
- commits
- pull requests
- successful builds
- passing tests
- working physical-device tests
- documentation alone

When communicating phase state, always distinguish:

**Phase / Branch Status** from **Feature / Refinement Status**.

The current canonical phase status is maintained in the [Phase Index](phases/README.md).

## ✅ Phase Completion Authority

Phase completion is **human-confirmed**, not AI-inferred.

AI may determine that a phase appears technically ready, but it must not declare the phase complete on its own.

The phase becomes officially complete only when the developer/product owner explicitly confirms acceptance. Examples include:

- "V1 Phase 15 is completed"
- "I accept V1"
- "V1 is complete"
- "This phase is complete"

Treat such an explicit statement as the authoritative completion signal.

Before that confirmation:

- do not change the phase status to **Established**, **Completed**, or equivalent
- do not present the phase as officially closed
- do not assume that a merged PR, successful build, passing tests, or working device test means the developer has accepted the phase

After explicit confirmation:

1. update the phase document status
2. update the phase index/status where applicable
3. update [CURRENT_STATE.md](CURRENT_STATE.md), [INDEX.md](INDEX.md), or [ROADMAP.md](ROADMAP.md) only when the confirmed completion materially changes those documents
4. update related decision status only when the decision was actually implemented and the phase completion provides the evidence
5. preserve the completion history rather than deleting the phase or its decisions

> **Technical readiness is evidence. Developer acceptance is completion.**

## 🤖 10. AI Ownership Boundary

AI may assist with:

- research
- reasoning
- architecture analysis
- implementation
- debugging
- testing ideas
- documentation

The developer owns:

- problem definition
- product direction
- key UX decisions
- architecture decisions
- financial correctness
- privacy decisions
- acceptance criteria
- final testing
- shipping decisions

> **AI accelerates the work. The developer owns the product.**

## 🔁 Standard New-Session Flow

```
Read project documentation
        ↓
Analyze Android
        ↓
Report Understanding
        ↓
Define Task
        ↓
Implement Focused Change
        ↓
Build / Test
        ↓
Review Product + Engineering
        ↓
Preserve Durable Knowledge
```

## 🧭 Conflict Resolution

When information conflicts:

1. Verify the current Android implementation.
2. Check the latest relevant Product decision.
3. Check current state and phase documentation.
4. Identify the conflict explicitly.
5. Do not silently rewrite product direction.
6. Record a new decision if the product direction genuinely changes.

> **The repository is the durable context layer. Conversations are temporary working sessions.**
