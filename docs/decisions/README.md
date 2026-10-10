# Product Decisions 🧩

> Durable decisions that explain **why TermRunway is shaped the way it is**.
>
> **Only accepted decisions belong here. Unresolved questions and decisions-in-progress belong in [issues](../issues/README.md).**

## 📌 Current Decisions

| ID | Decision | Why it matters |
|---|---|---|
| [001](001-native-android-direction.md) | Native Android Direction | Defines the current product platform |
| [002](002-offline-first.md) | Offline First | Protects privacy, reliability and local ownership |
| [003](003-two-mode-product-model.md) | Two-Mode Product Model | Connects actual tracking with future planning |
| [004](004-product-knowledge-repository.md) | Separate Product Knowledge Repository | Preserves product context independently of implementation |
| [005](005-analysis-first-ai-handoff.md) | Analysis-First AI Development Handoff | Gives new AI sessions durable context before implementation |
| [006](006-saving-goals.md) | Saving Goals | Adds explicit future-saving intent without creating a third product mode |
| [007](007-incremental-architecture-evolution.md) | Incremental Architecture Evolution | Keeps architecture adaptable without speculative rewrites |

## 🗃️ Decision Categories

**🎯 Product** — users, problem, scope, priorities, direction

**🧭 UX** — navigation, hierarchy, interaction, guidance

**🏗️ Architecture** — platform, data ownership, boundaries, constraints

**🔐 Privacy / Data** — local storage, backup, data collection, network requirements

**📝 Documentation** — repository responsibilities, README policy, AI continuity

## ✍️ Decision Template

```markdown
# Decision: <title>

## Context
What problem or uncertainty required a decision?

## Options Considered
What realistic alternatives existed?

## Decision
What was chosen?

## Why
Why was it chosen?

## Consequences
What becomes easier or harder?

## Status
Accepted / Superseded / Revisit Later

## Related
- Evidence / Investigation:
- Phase:
- Implementation:
```

> **Record rationale, not bureaucracy.**

The purpose of a decision record is to prevent important product reasoning from disappearing into old conversations.
