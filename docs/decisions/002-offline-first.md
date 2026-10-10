# Decision 002: Offline-First Product Architecture

## Context

TermRunway handles personal financial information and should remain useful without depending on a network connection.

## Decision

Core TermRunway functionality should work locally and offline.

No backend or cloud service is required for the core product.

## Why

This provides:

- stronger privacy
- simpler architecture
- better reliability
- no account dependency
- useful operation in poor connectivity
- local ownership of financial data

## Consequences

Features requiring synchronization or remote processing cannot be added casually.

Any future cloud capability requires a new explicit product and privacy decision.

## Status

**Accepted**

## Related

- [Product Rules](../PRODUCT_RULES.md)
