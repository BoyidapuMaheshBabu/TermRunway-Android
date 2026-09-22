# TermRunway Android 💸

> **A student-focused Android app for planning and understanding money across a semester or selected period.**

TermRunway is an offline-first student finance application designed to help students understand available money, planned expenses, income, actual spending, and their financial runway.

This repository contains the **native Android version of TermRunway**, developed from the lessons and product ideas explored in the original web version.

## ⭐ Highlights

- Student-focused financial planning and tracking
- **Plan Mode** for forecasting a semester or selected period
- **Tracking Mode** for recording actual income and expenses
- Projected and remaining balance calculations
- Financial insights based on planned or recorded values
- Native Android mobile experience
- Offline-first product direction without requiring a mandatory account
- Practical workflow focused on understanding money rather than banking

## 💡 What It Does

TermRunway is built around two ways of working with student finances.

| Mode | Main question | Purpose |
| --- | --- | --- |
| **Tracking Mode** | What happened? | Record actual income and expenses and understand spending. |
| **Plan Mode** | What will happen? | Plan money for a semester or selected period using available money, expected income, and planned expenses. |

### Tracking Mode

Tracking Mode focuses on real transactions.

Students can record information such as:

- Income
- Food expenses
- Transport expenses
- Accommodation expenses
- Other spending

The goal is to understand actual spending over time.

> **Record what happened and understand it.**

### Plan Mode

Plan Mode is for students who want to estimate how their money may behave during a future period.

Typical planning inputs include:

- Start and end dates
- Available money
- Expected income
- Planned expenses
- Expense categories

A basic projection is:

```text
Projected Balance
= Available Money
+ Expected Income
- Planned Expenses
```

As the plan changes, the app can use the updated values to show the effect on the student's financial runway.

> **Plan ahead and understand the financial impact.**

## 📊 Insights

TermRunway brings the results of planning and tracking into a student-friendly view.

### Tracking Insights

- Actual income and expenses
- Spending by category
- Spending over a selected period
- Income vs. expense information
- Historical spending patterns

### Planning Insights

- Projected balance
- Expected income and expenses
- Planning-period outlook
- Financial status and warnings
- Effect of changing planned expenses

The purpose is not to behave like a banking application. It is to help students understand their own financial situation and make practical plans.

## 🧠 What I Learned

TermRunway Android is a practical learning project built by identifying product requirements, learning unfamiliar concepts, implementing them, testing the behavior, and improving the application.

Key areas explored:

- Kotlin and Android development
- Jetpack Compose UI development
- Material 3 components and design
- State-driven UI and user input handling
- Financial calculations and application logic
- Validation and edge cases
- Mobile-first product design
- Translating a web idea into a native Android application
- Maintaining and improving a real project

## 🤖 AI-Assisted Development

TermRunway Android was developed with AI assistance.

I use AI as a development and learning tool to:

- explore unfamiliar Android concepts
- understand implementation options
- generate or modify code when needed
- investigate errors
- iterate on features and UI
- review and improve the implementation

The project is **not presented as line-by-line manual coding without AI assistance**. The focus is on understanding the product, making implementation decisions, testing the result, and learning from each iteration.

## 🛠️ Technology

- Kotlin
- Jetpack Compose
- Material 3
- Android SDK
- AndroidX
- Gradle
- Git
- GitHub
- Android Studio

## 📁 Project Structure

```text
TermRunway-Android/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/termrunway/
│       │   └── res/
│       ├── test/
│       └── androidTest/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

The main Android application code is under `app/src/main`, with separate source sets for unit tests and Android instrumentation tests.

## 🔄 Development Approach

The project follows a practical development cycle:

```text
Understand the problem
        ↓
Define requirements
        ↓
Design the workflow
        ↓
Learn what is needed
        ↓
Implement
        ↓
Test
        ↓
Debug
        ↓
Improve
```

The Android application is being developed from the product idea itself rather than simply copying the previous web interface screen by screen.

## 🧪 Testing & Improvements

The project includes Android unit-test and instrumentation-test source sets.

As features are added, the application is tested through:

- build and runtime checks
- UI behavior checks
- financial calculation validation
- input and edge-case testing
- iterative fixes and refinements

The goal is to keep improving both the product behavior and the implementation quality as the project grows.

## 🔮 Roadmap

Planned improvements may include:

- richer financial insights
- deeper tracking and planning workflows
- more detailed spending analysis
- improved accessibility
- continued UI and usability refinements
- stronger testing as the application grows

The roadmap may change as the product develops.

## 🔗 Related Project

Web version: [TermRunway](https://github.com/BoyidapuMaheshBabu/TermRunway)

## 👨‍💻 Developer

**Boyidapu Mahesh Babu**  
Diploma in Computer Science Engineering student

GitHub: [@BoyidapuMaheshBabu](https://github.com/BoyidapuMaheshBabu)

---

**TermRunway — understand your money, plan your runway.**
