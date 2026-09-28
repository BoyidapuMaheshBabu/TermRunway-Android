# TermRunway Android 📱

> **A native Android student-finance app focused on helping students understand their spending through practical tracking and planning.**

TermRunway Android is the native Android continuation of my TermRunway project, rebuilt with **Kotlin and Jetpack Compose** as an offline-first mobile application.

## ⭐ Highlights

- Daily income and expense tracking
- Home dashboard with current financial activity
- Day-by-day transaction tracking
- Calendar-based date selection
- Add, edit, and delete transactions
- 7 / 30 / 90-day spending and income insights
- Category-based spending breakdowns
- Local JSON backup and restore
- No account or server required
- Physical-device testing during development
- Built incrementally through real implementation problems

## 💡 Why Android?

TermRunway originally started as a web application.

The web version helped me explore budgeting logic, calculations, validation, persistence, responsive UI, and deployment. As the project grew, I wanted to take the idea further as a real mobile application rather than continuing to expand the original web implementation.

The Android version gives TermRunway a more direct student-focused experience:

```text
TermRunway Web
      ↓
Learned the product idea
      ↓
Encountered limitations around deployment, persistence, and future expansion
      ↓
Re-thought the architecture
      ↓
TermRunway Android
      ↓
Offline-first native application
```

The goal is not simply to convert the old website into an app. The Android project is a rebuild where the product structure, architecture, and implementation can be improved as I learn.

## 🎯 What TermRunway Is

TermRunway is designed around a simple student-finance problem:

> **How can a student understand where their money is going and make better day-to-day spending decisions?**

The project is planned around two major modes:

### Daily Tracking

For students who do not have a fixed budget or long-term plan.

The user can track income and expenses and review spending across different periods.

### Term Mode

For students who want to plan money across a semester or another fixed period.

The user can define expected income, planned expenses, dates, and then compare the plan with actual financial activity.

**Term Mode is intentionally not active in the current rebuild.** Daily Tracking is being developed first so the core tracking loop can become stable before the planning system is added.

## 📱 Current Product Scope

The active product is **Daily Tracking**.

### Home

- Today's spending
- Today's income
- Net amount
- Recent activity
- Quick actions

### Track

- Day-by-day navigation
- Calendar selection
- Add transactions
- Edit transactions
- Delete transactions

### Insights

- 7-day summaries
- 30-day summaries
- 90-day summaries
- Spending and income totals
- Category breakdowns

### Settings

- Profile name
- Informational daily limit
- Theme
- Backup and restore
- Local data reset

### Storage

Data is stored locally on the device using JSON.

No account, backend, or server is required for the current version.

## 🧠 What I Am Learning

TermRunway Android is also a practical Android-development learning project.

Through the project I am exploring:

- Kotlin
- Jetpack Compose
- Material 3
- UI state management
- Application architecture
- Local persistence
- Data validation
- Financial calculations
- Date and money handling
- Backup and restore design
- Git and GitHub workflows
- Branches, commits, and pull requests
- Testing on a physical Android device
- Debugging real implementation problems

The focus is not on learning technologies separately. I learn what is required when the project reaches a problem that needs it.

## 🤖 AI-Assisted Development

TermRunway Android is developed with AI assistance.

I use AI as a development and learning tool to:

- explore unfamiliar Android concepts
- investigate implementation problems
- understand errors
- iterate on UI and application logic
- review possible approaches
- debug issues during development

AI assistance does not mean the project is presented as manually written line-by-line without AI. The important part of the process is understanding the implementation, testing it on the device, encountering problems, and improving the application.

My development approach is:

```text
Build
  ↓
Encounter a Problem
  ↓
Explore / Learn
  ↓
Fix
  ↓
Test
  ↓
Improve
```

## 🛠️ Technology

- Kotlin
- Jetpack Compose
- Material 3
- Android Studio
- Local JSON storage
- Git
- GitHub

The current version is intentionally **offline-first**.

There is no Firebase, Supabase, authentication, cloud database, or external API dependency in the current product scope.

## 📁 Project Structure

```text
TermRunway-Android/
│
├── data/
│   └── Models, local persistence, and backup format
│
├── domain/
│   └── Financial calculations and business logic
│
├── ui/
│   ├── components/
│   │   └── Reusable finance UI and transaction editor
│   │
│   ├── screens/
│   │   └── Home, Track, Insights, Settings, and first-run setup
│   │
│   ├── navigation/
│   │   └── Bottom navigation
│   │
│   └── theme/
│       └── Visual system
│
├── util/
│   └── Date and money formatting
│
└── README.md
```

The screen layer owns UI state, while persistence is handled through `TermRunwayRepository`.

The repository validates data before writing and fully parses a backup before a restore can replace local data.

## 🧪 Testing & Improvements

The application is tested during development on a **physical Android device**.

Testing focuses on whether the complete user flow actually works rather than only checking whether individual screens look correct.

Areas being improved include:

- transaction creation and editing
- transaction deletion
- date handling
- financial calculations
- validation
- local persistence
- backup and restore
- dashboard summaries
- period-based insights
- UI state and navigation
- real-device behavior

## 🔄 Development Workflow

I am developing the project incrementally rather than trying to build the entire application at once.

```text
Understand the requirement
        ↓
Build one part
        ↓
Encounter a problem
        ↓
Explore and learn
        ↓
Fix the problem
        ↓
Test on device
        ↓
Improve
        ↓
Commit
```

GitHub branches, commits, and pull requests are used to keep the development process closer to a real software-development workflow.

## 🔮 Future Direction

After Daily Tracking becomes stable, the next major direction is **Term Mode**.

Possible future areas include:

- semester/term planning
- planned vs actual spending
- runway calculations
- expected income
- planned expense categories
- health/status indicators
- richer financial insights

Other ideas may be explored later, but they will be added only when they make sense for the product.

## 📌 Status

**Active development**

The current rebuild is focused on completing and stabilizing **Daily Tracking** before expanding into Term Mode.

## 👨‍💻 Developer

**Boyidapu Mahesh Babu**  
Diploma in Computer Science Engineering student

GitHub: [@BoyidapuMaheshBabu](https://github.com/BoyidapuMaheshBabu)

---

**Built incrementally as a practical Android development and software-engineering learning project.**
