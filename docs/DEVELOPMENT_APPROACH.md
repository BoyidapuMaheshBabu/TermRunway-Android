# Development Approach 🧭

> **Understand → Decide → Build → Validate → Improve**
>
> TermRunway uses a product-driven, phase-based development workflow where AI accelerates execution, while product decisions and final validation remain human-owned.

---

## ⚡ At a Glance

| Stage | What happens |
|---|---|
| 🎯 **Problem** | Define the real user problem |
| 🔎 **Research** | Explore UX, architecture, privacy, and edge cases |
| 🧠 **Decide** | Choose what the product should do |
| 📝 **Document** | Preserve important decisions |
| 🗂️ **Plan** | Scope the work into a focused phase |
| 🛠️ **Build** | Implement with AI-assisted development |
| 🧪 **Validate** | Build, debug, test, and use a physical device |
| 🔁 **Review** | Check both correctness and product quality |
| 🚀 **Improve** | Refine the product and preserve durable learning |

The goal is not to produce more process. It is to keep **implementation connected to product intent**.

---

## 🎯 1. Start With the Problem

Development starts with:

> **“What problem are we solving?”**

—not:

> “What code should I write?”

For TermRunway, the core problem is helping students understand money in relation to **time, actual spending, and planned spending**.

That led to two complementary modes:

- 📊 **Daily Tracking** — *What happened?*
- 🗓️ **Plan Tracking** — *What will happen?*

Product direction is decided before implementation details.

---

## 🔎 2. Research Before Committing

When a product question is unclear, I research before locking in an implementation.

Research may cover:

- 👤 user needs and workflows
- 🎨 UX patterns
- 🏗️ architecture options
- 🔐 privacy and data implications
- ⚠️ edge cases
- ⚖️ simplicity vs. complexity
- 💡 whether a feature is actually valuable

The output is intentionally lightweight:

**Question → Evidence → Conclusion → Decision**

Long research conversations are not treated as the source of truth. Important conclusions are promoted into the documentation in `docs/`.

---

## 🧩 3. Keep Product Thinking and Implementation Together

TermRunway now uses **one repository with clear boundaries between documentation and implementation**.

| Location | Owns |
|---|---|
| 📚 `docs/` | Product context, decisions, phases, roadmap, rules, AI handoff, methodology, and durable product knowledge |
| 📱 Repository root and `app/` | Kotlin/Compose implementation, tests, build configuration, and release work |

> **`docs/` = why the product behaves as it does.**  
> **Android source, tests, and build files = how it is implemented.**

Keeping both in one repository reduces context splitting while preserving a clear distinction between product decisions and implementation details.

---

## 🗂️ 4. Work in Focused Phases

Large changes are divided into focused development phases instead of mixing unrelated work.

A phase normally defines:

**Objective → Requirements → Implementation → Testing → Review → Closure**

Examples include:

- 🏗️ architecture foundation
- 🧭 navigation and home experience
- ⌨️ input UX
- 💰 data entry and categories
- 🗓️ plan and semester experience
- 🛡️ backup and data safety
- ✨ UI/UX polish
- ⚙️ performance and reliability
- 🧪 testing and release readiness
- 🚀 V1 finalization

Phases exist to create **scope and traceability**, not to create process for its own sake.

### 🔢 Major-Version Phase Numbering

Phase numbers are scoped to the major product version.

- The current cycle is **V1 Phase 01–15**.
- **V1 Phase 15 — V1 Finalization** is the end of the V1 sequence.
- Do not create V1 Phase 16.
- After V1 is explicitly completed and accepted, the next major cycle begins at **V2 Phase 01**.

### 🌿 Phase Branch Discipline

TermRunway follows a **one-phase-at-a-time branch workflow**.

The documentation in `docs/` records the canonical phase names and the exact Android branch names. The Android repository creates only the branch for the phase that is actually beginning.

Rules:

1. **Use the exact branch name recorded in the documentation in `docs/`.**
2. **Use the major-version prefix:** V1 uses `v1/phase/...`; V2 will use `v2/phase/...`.
3. **Create only the current phase branch.** Do not pre-create future phase branches.
4. **Start the phase branch from the latest `main`** in TermRunway-Android.
5. **Keep the phase scope focused.** Do not mix future-phase work into the current branch.
6. **After implementation, testing, review, and developer acceptance, merge the phase branch into `main`.**
7. **Freeze the completed phase branch as a historical checkpoint.** Do not merge later `main` changes back into old phase branches.
8. **Then create the next phase branch from the new `main`.**

Canonical flow:

```text
Latest main
   ↓
Create current phase branch
   ↓
Implement
   ↓
Test + Review
   ↓
Developer Acceptance
   ↓
Merge → main
   ↓
Freeze phase branch
   ↓
Create next phase branch from latest main
```

> **Product defines the phase and branch name. Android creates the branch only when that phase begins.**

---

## 🌿 5. GitHub Preserves the Engineering Trail

GitHub is more than a place to store the final code.

It preserves:

**Decision → Phase → Branch → Implementation → Commit → Test → Review**

Branches provide focused development. Commits preserve meaningful change history. The documentation in `docs/` preserves decisions that should survive beyond a single conversation.

This makes the project easier to inspect, continue, and explain.

---

## 🤖 6. AI-Assisted Development

AI is a development accelerator—not the product owner.

### 💡 Product & reasoning
AI helps with:

- exploring ideas
- comparing options
- finding edge cases
- challenging assumptions
- organizing research
- structuring decisions

### 🛠️ Implementation
AI can accelerate:

- code generation
- debugging
- refactoring
- repetitive implementation
- unfamiliar Android concepts
- documentation

### 🧪 Review & learning
AI can help explain:

- why an implementation works
- what caused a bug
- trade-offs
- what should be tested
- possible improvements

The boundary is simple:

> **AI accelerates the work. I decide what should be built and whether the result is correct.**

Generated implementation is reviewed, built, tested, and evaluated before it becomes part of the product.

---

## 🔄 7. New AI Sessions Start From Durable Context

Development may happen across different AI accounts and tools.

A new session therefore follows:

**📘 Read Product Knowledge → 📱 Analyze Android → 🧠 Report Understanding → 🎯 Define Task → 🛠️ Implement → 🧪 Test → 🔍 Review**

A new AI session should understand the current product state before making changes.

It should **not** repeatedly reconstruct the project from old chat history.

> **AI context may change. Product knowledge should not.**

See [AI_HANDOFF.md](AI_HANDOFF.md) for the detailed handoff process.

---

## 🧪 8. Testing Is Part of Development

A feature is not finished because it compiles.

Validation includes:

- ✅ build and compile checks
- 🐛 debugging
- 🔢 financial-calculation checks
- ⚠️ edge-case validation
- 📱 physical-device testing
- 👀 UX review

TermRunway is phone-first, so a real Android device is part of the development loop—not only the final demo.

---

## 🔍 9. Review the Product, Not Just the Code

After implementation, two questions matter:

> **Does it work?**

and

> **Does it improve the product?**

Review can uncover:

- confusing UX
- incorrect calculations
- unnecessary complexity
- inconsistent screens
- privacy concerns
- missing edge cases
- outdated documentation
- product decisions that need to be recorded

A technically working feature is not automatically a good product feature.

---

## 📝 10. Document What Should Survive

Not every thought needs documentation.

Durable information does.

Examples:

- 🎯 product decisions
- 🏗️ architecture choices
- 🔎 research conclusions
- 🗂️ phase requirements
- 📏 product rules
- 📍 current state
- 🤖 AI handoff guidance
- 🧭 development methodology

> **Conversation is temporary. Decisions are durable.**

---

## 🧰 11. Tools Have Roles — The Workflow Is the System

| Tool | Role |
|---|---|
| 🤖 AI assistants | Research, reasoning, debugging, implementation assistance |
| 🐙 GitHub | Version control, history, durable knowledge |
| 📚 `docs/` | Product decisions and planning |
| 📱 Repository root and `app/` | Working implementation |
| 🛠️ Android Studio | Build, debug, run |
| 📲 Physical device | Real-world validation |
| 🎨 Canva | Presentation and visual communication |

The tools are replaceable. The **workflow and ownership model** are the important part.

---

## 🛡️ 12. What Remains Human-Owned

AI and automation are useful, but important decisions remain mine:

- 🎯 problem definition
- 🧭 product direction
- 🎨 key UX decisions
- 🏗️ architecture choices
- 💰 financial-calculation correctness
- 🔐 privacy decisions
- ✅ feature acceptance
- 📱 final testing
- 📝 durable documentation
- 🚀 what gets shipped

This keeps TermRunway **AI-assisted, not AI-owned**.

---

## 📈 13. What TermRunway Taught Me

Building TermRunway expanded my focus from simply writing code to thinking about:

**Architecture · Product requirements · UX · Git · Testing · Documentation · AI-assisted engineering · Real-device validation · Trade-offs**

The important outcome is not only the application.

It is learning how to take an idea, turn it into a product, and improve that product through a repeatable engineering process.

---

## 🚀 The Complete Loop

```
🎯 Problem
   ↓
🔎 Research
   ↓
🧠 Decide
   ↓
📝 Document
   ↓
🗂️ Plan Phase
   ↓
🤖 AI-Assisted Build
   ↓
🧪 Build & Debug
   ↓
📱 Real-Device Validation
   ↓
🔍 Product Review
   ↓
🔁 Improve & Record
   ↺
```

### The principle

> **Understand first. Decide clearly. Build efficiently. Validate the real product. Preserve what matters.**

That is the development approach behind TermRunway.
