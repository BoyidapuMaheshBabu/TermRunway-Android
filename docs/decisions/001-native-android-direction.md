# Decision 001: Native Android as the Current Product

## Context

TermRunway began as a web prototype using a simple frontend and local storage.

As the product evolved, the desired experience became a private, offline-first student finance application used directly on a phone.

## Options Considered

1. Continue the web application as the main product.
2. Add a backend/cloud layer to extend the web application.
3. Build a native Android application focused on local, offline use.

## Decision

**Native Android is the current product direction.**

The Android implementation is maintained in the TermRunway-Android repository.

## Why

The Android direction better matches:

- phone-first student usage
- offline reliability
- local financial-data ownership
- simpler privacy model
- native Android interaction
- direct physical-device testing

## Consequences

The web prototype is no longer the active implementation.

It remains public as historical product-development evidence.

## Status

**Accepted**

## Related

- [Product Context](../PRODUCT_CONTEXT.md)
