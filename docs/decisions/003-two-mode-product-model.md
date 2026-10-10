# Decision 003: Two-Mode Product Model

## Context

TermRunway needs to support both understanding past spending and planning future money use.

Treating them as unrelated features would fragment the product.

## Decision

TermRunway uses two complementary modes:

### Daily Tracking
**What happened?**

### Plan Tracking
**What will happen?**

Plan Tracking is a core product capability, not merely a secondary visualization.

## Why

The two modes represent the natural financial lifecycle:

```
Actual money movement
        ↓
Understanding
        ↓
Planning
        ↓
Future guidance
        ↓
Actual results
        ↺
```

The planning experience should use the same underlying financial reality as the tracking experience.

## Consequences

- Planning must remain connected to actual transactions.
- Navigation should make the distinction understandable.
- Product development should not create two independent financial systems.

## Status

**Accepted**

## Related

- [Product Context](../PRODUCT_CONTEXT.md)
- [Product Rules](../PRODUCT_RULES.md)
