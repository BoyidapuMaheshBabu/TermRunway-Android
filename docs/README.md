# TermRunway Product & Engineering Documentation 🧭

> **Your money. Your semester. Your runway.**

This folder preserves TermRunway's durable product context, UX rules, decisions, development phases, roadmap, AI handoff, and project workflow. It lives inside the same repository as the Android implementation so the product's intent and code have one source of truth.

## Current Product

**TermRunway Android 1.0.0** is the active V1 product: an offline-first native Android student finance tracker with Daily Tracking and Plan Tracking.

- **Daily Tracking** — understand what happened.
- **Plan Tracking** — understand what is expected over a planning period.
- **Privacy direction** — data stays local; no account, bank connection, or required backend.
- **Engineering direction** — Kotlin, Jetpack Compose, local persistence, integer paise for monetary values, and real-device validation.

V1 uses phases 01–15. V1 phases 01–10 are completed; Phase 11 is the current priority according to the project state recorded in these documents. Saving Goals remain future scope unless their status is explicitly updated.

## Start Here

| Document | Purpose |
|---|---|
| [INDEX.md](INDEX.md) | Main documentation navigation and phase status |
| [PRODUCT_CONTEXT.md](PRODUCT_CONTEXT.md) | Product identity, problem, audience, and principles |
| [CURRENT_STATE.md](CURRENT_STATE.md) | Current product and development state |
| [PRODUCT_RULES.md](PRODUCT_RULES.md) | Non-negotiable product and UX rules |
| [ROADMAP.md](ROADMAP.md) | Direction and future scope |
| [DEVELOPMENT_APPROACH.md](DEVELOPMENT_APPROACH.md) | Product-driven, phase-based workflow |
| [AI_HANDOFF.md](AI_HANDOFF.md) | How a new AI session should regain context |
| [README_POLICY.md](README_POLICY.md) | When documentation should be updated |
| [decisions/README.md](decisions/README.md) | Decision register |
| [issues/README.md](issues/README.md) | Open questions and unresolved decisions |
| [phases/README.md](phases/README.md) | Phase plan and status details |
| [PPT.md](PPT.md) | Presentation content reference |

## One Repository, Clear Boundaries

- **Repository root and `app/`** — Android implementation, tests, build configuration, and release work.
- **`docs/`** — product context, accepted decisions, phase plans, unresolved questions, roadmap, AI handoff, and durable engineering workflow.

Conversation history is temporary; durable decisions belong here. Not every discussion needs a document—record information when it materially affects product direction, rules, architecture, scope, or future work.

## Development Loop

```
Problem → Research → Decision → Documentation → Phase Plan
       → Android Implementation → Build/Test → Device Validation → Review
```

AI may assist with research and implementation, but the developer owns product decisions, validation, acceptance, and shipping.

---

**Understand first. Decide clearly. Build efficiently. Validate the real product. Preserve what matters. 🚀**
