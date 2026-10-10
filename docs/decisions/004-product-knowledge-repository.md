# Decision 004: Separate Product Knowledge Repository
> **Status: Superseded.** This decision originally established a separate product-knowledge repository. The documentation has since been consolidated into `TermRunway-Android/docs/` so product context and implementation can be maintained in one repository. Keep this record as decision history; its original repository separation is no longer the current structure.


## Context

TermRunway is researched and developed across multiple AI accounts, devices, and development tools.

Important decisions were previously at risk of remaining inside temporary conversations.

## Decision

Use **TermRunway documentation** as the durable product-knowledge layer.

The Android repository remains the implementation source of truth.

## Responsibilities

### TermRunway documentation

Owns:

- product context
- roadmap
- research
- decisions
- phase planning
- product rules
- documentation policy
- future scope

### TermRunway-Android

Owns:

- source code
- Android architecture implementation
- tests
- build configuration
- implementation documentation
- release artifacts

## Why

This separation allows different AI tools and accounts to understand the product without requiring the original conversation history.

## Core Rule

> If a decision matters to the product, preserve the decision in GitHub.

## Status

**Accepted**

## Related

- [Index](../INDEX.md)
- [Product Context](../PRODUCT_CONTEXT.md)
