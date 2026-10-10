# Product Issues & Unresolved Decisions 🧩

> A durable place for product questions, unresolved decisions, and discussions that are important but **not ready to become accepted decisions or implementation phases**.

## Purpose

Use this area when TermRunway has a real product question that still needs thinking.

Examples:

- an important feature idea that is not fully defined
- competing product directions
- unclear UX behavior
- unresolved data/model questions
- decisions that need more research
- ideas that may become a future phase but are not ready yet

This folder prevents unresolved thinking from disappearing into conversations while keeping the accepted `decisions/` folder clean.

## Status Model

Use these states:

- **Open** — problem/question is being investigated
- **Discussion** — possible directions are being explored
- **Researching** — evidence is being collected
- **Decision Ready** — enough clarity exists to make a decision
- **Decided** — the decision has been promoted into `decisions/`
- **Rejected / Deferred** — intentionally not pursued for now

## Lifecycle

```
Issue / Question
      ↓
Discussion
      ↓
Research
      ↓
Decision Ready
      ↓
Accepted Decision
      ↓
decisions/
      ↓
Phase (only when implementation-ready)
```

## Rules

- Do not treat an issue as a product requirement.
- Do not assign a phase number until the behavior is sufficiently clear.
- Do not move an unresolved issue into `decisions/` just to make it look complete.
- Preserve the reasoning and open questions.
- When a decision is accepted, create or update the appropriate record in `decisions/`.
- When implementation is ready, create or update the appropriate phase documentation.
- Temporary brainstorming that has no durable product value does not need to be stored here.

> **Unresolved thinking belongs here. Accepted reasoning belongs in `decisions/`. Implementation-ready scope belongs in `phases/`.**
