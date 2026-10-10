# README Policy

This document defines when the main **TermRunway-Android README** should change.

## Purpose

The README should represent the product clearly without becoming a constantly changing development diary.

## Update the Android README When

Update it when there is a material change to:

- product purpose
- core modes
- major user-facing functionality
- architecture that users/developers need to understand
- privacy model
- installation/build requirements
- major screenshots
- release/version information
- stable project structure
- final product capabilities

## Do Not Update It For

Do not redesign the README for:

- minor UI spacing changes
- small bug fixes
- refactoring
- temporary experiments
- internal variable/file changes
- every phase commit
- small implementation details
- unfinished ideas

## V1 Rule

The large README redesign should happen **after V1 finalization**.

The final README should be based on the stable product.

## Planned Final README Structure

The future major redesign should prioritize scanning:

1. Product title + tagline
2. Logo / hero visual
3. One-sentence value proposition
4. Key highlights
5. Problem solved
6. Core features
7. How TermRunway works
8. Screenshots
9. Privacy / offline model
10. Tech stack
11. Architecture / engineering details
12. AI-assisted workflow
13. Testing / validation
14. Development history
15. Future direction

The README should remain useful to:

- a student evaluating the project
- a recruiter
- a developer reading the repository
- a presenter reviewing the product
- an AI agent needing high-level product context

## Source of Truth

The Android repository is the source of truth for **implementation**.

This repository is the source of truth for **product knowledge and decisions**.

If they disagree:

1. determine whether the implementation changed intentionally
2. document the decision if necessary
3. update the appropriate source
4. do not silently let the two repositories drift
