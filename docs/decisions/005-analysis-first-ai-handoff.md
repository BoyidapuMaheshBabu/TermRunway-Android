# Decision 005: Analysis-First AI Development Handoff 🤖

## Context

TermRunway is developed across different AI tools, accounts, devices, and development environments.

Without a durable handoff process, a new AI session may begin implementing from incomplete context, old assumptions, or a temporary conversation.

The documentation in `docs/` already serves as the durable product knowledge layer, so AI sessions need a consistent way to recover that context before implementation.

## Options Considered

### 1. Depend on previous conversations
**Rejected.** Conversations are temporary and may not be available across tools, accounts, or devices.

### 2. Give every AI session a large prompt containing the entire project
**Rejected.** It creates duplication, becomes stale, wastes context, and does not establish a durable source of truth.

### 3. Let AI inspect only the Android code
**Rejected.** Code explains implementation, but not always the product problem, decisions, constraints, or future direction.

### 4. Product knowledge first, implementation analysis second
**Chosen.** The documentation in `docs/` provides durable intent, and the Android repository provides current implementation reality.

## Decision

TermRunway uses an **analysis-first AI development handoff**.

Before a meaningful implementation task, a new AI session should:

1. Read relevant documentation in `docs/` knowledge.
2. Analyze the current Android implementation.
3. Report its understanding.
4. Define the focused task and acceptance criteria.
5. Implement only the approved scope.
6. Build and test the change.
7. Review both engineering correctness and product fit.
8. Preserve any new durable knowledge.

The detailed process is defined in [AI_HANDOFF.md](../AI_HANDOFF.md).

## Why

This keeps AI assistance connected to product intent without making AI the owner of product decisions.

It also prevents the project from becoming dependent on any single AI tool or conversation history.

The model is:

> **Product knowledge = durable intent**  
> **Android repository = implementation reality**  
> **AI session = temporary working context**

## Consequences

### Positive

- New AI sessions can regain context consistently.
- Product decisions survive changes in AI tools and accounts.
- Implementation starts from current reality instead of assumptions.
- AI can work efficiently without copying entire conversation histories.
- Human ownership of product decisions remains explicit.
- The GitHub repositories become a durable engineering trail.

### Trade-offs

- AI sessions must spend some time reading context before implementation.
- Documentation must be kept accurate when durable decisions change.
- Conflicts between product documentation and implementation must be surfaced and resolved intentionally.

## Status

**Accepted**

## Related

- Product knowledge repository: [Decision 004](004-product-knowledge-repository.md)
- Development workflow: [DEVELOPMENT_APPROACH.md](../DEVELOPMENT_APPROACH.md)
- AI handoff: [AI_HANDOFF.md](../AI_HANDOFF.md)
- Current product state: [CURRENT_STATE.md](../CURRENT_STATE.md)
