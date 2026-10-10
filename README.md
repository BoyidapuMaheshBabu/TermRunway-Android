<p align="center">
  <img
    src="docs/termrunway-logo.jpeg"
    alt="TermRunway — Plan, Calculate, Achieve"
    width="650"
  />
</p>

<h1 align="center">TermRunway 1.0.0 🛫💸</h1>

<p align="center">
  <strong>Your money. Your semester. Your runway.</strong><br>
  An offline-first Android app for student financial tracking and semester planning.
</p>

<p align="center">
  <a href="docs/INDEX.md">Product & Engineering Docs</a> ·
  <a href="https://github.com/BoyidapuMaheshBabu/TermRunway-Web">Original Web Prototype</a>
</p>

---

## 🚦 Product at a Glance

| | |
|---|---|
| 📱 Platform | Native Android |
| 🧩 UI | Kotlin + Jetpack Compose + Material 3 |
| 🔐 Data | Local / privacy-focused |
| 📡 Core connectivity | Offline |
| 👤 Account | None required |
| ☁️ Backend / cloud | Not required |
| 🏦 Bank connection | None |
| 💰 Currency safety | Integer paise internally |
| 🧪 Validation | Unit tests + physical-device testing |
| 🔢 Version | **1.0.0** |

## 🧭 Two Modes, One Financial Reality

### 💸 Daily Tracking — *What happened?*

- Record income and expenses
- Review daily, weekly and monthly activity
- Search and filter transactions
- View spending by category
- View simple spending trends

### 🗓️ Plan Tracking — *What will happen?*

- Choose a custom planning period
- Record money available at the start
- Add expected income
- Add planned expenses
- See expected remaining money while planning
- Compare actual activity with the plan
- Calculate remaining money per day
- Receive contextual spending-pace guidance

> **The plan is connected to the same underlying transaction reality — not a separate calculator.**

## 🏗️ Architecture

```
Jetpack Compose
      ↓
ViewModel
      ↓
FinancialCalculator / Repository
      ↓
Local SQLite

Small preferences → DataStore

Backup / Restore
      ↓
Android Storage Access Framework
      ↓
JSON
```

### 💰 Financial correctness

Money values are represented internally as **integer paise** to avoid floating-point currency errors.

### 🔒 Privacy by design

TermRunway is deliberately offline-first:

- no account
- no backend
- no cloud sync
- no bank connection
- no financial API
- no required internet connection

The app collects only the user's name for the local experience.

## 🧪 Development & Validation

TermRunway follows:

**Problem → Research → Decide → Document → Phase → Build → Test → Review → Improve**

AI is used as a **development accelerator and learning assistant** for implementation, debugging, research, documentation and exploration.

> **AI accelerates the work. Product decisions, testing and final engineering judgment remain human-owned.**

Validation includes configured **GitHub Actions** checks for debug builds and unit tests, plus testing on physical Android devices.

## 📚 Product & Engineering Documentation

Product context, decisions, phases, roadmap, and AI handoff are maintained in the [documentation index](docs/INDEX.md), alongside the Android implementation.

## 🛠️ Development Approach

The project is built around:

```
Understand
   ↓
Decide
   ↓
Build
   ↓
Validate
   ↓
Improve
```

Quality is prioritized over speed, and real-device behavior matters more than a successful build alone.

## 📈 Project Evolution

```
Web Prototype
     ↓
Build + Test
     ↓
Identify Product Limitations
     ↓
Reconsider Architecture
     ↓
Native Android Product
     ↓
V1 Finalization
```

### Why `TermRunway-Web` remains public

The Web repository is intentionally preserved as a **historical prototype and product-evolution record**, not as a second active product. It shows how the original budgeting idea was tested, where the approach stopped fitting the intended student experience, and why the product direction changed.

The prototype helped reveal several constraints:

- Browser `localStorage` was not the preferred long-term foundation for the intended app.
- The experience depended on the browser environment, while phone-first usage became the priority.
- Strong offline behavior and keeping financial data local became core product requirements.
- Continuing to add features to the web version would have extended an architecture that no longer fit the product direction.

Rather than hide or erase that earlier work, the repository preserves the original implementation and the lessons learned from it. It is useful for tracing the product's history and explaining the engineering trade-off. **The native Android repository remains the only active implementation and source of truth.**

👉 [TermRunway-Web — Historical Prototype](https://github.com/BoyidapuMaheshBabu/TermRunway-Web)

## 👨‍💻 Developer

**Boyidapu Mahesh Babu**  
Diploma in Computer Science Engineering student

---

> **Understand first. Build deliberately. Test the real product. Improve continuously. 🚀**
