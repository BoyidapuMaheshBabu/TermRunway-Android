# TermRunway PPT Content & Presentation Thinking

This document records the agreed presentation structure for the college PPT. It captures not only what each section contains, but also the intended story: **product → real problem → existing approaches → proposed solution → architecture → technology → features → working flow → advantages → future scope → conclusion**.

> Scope: Slides/content 1–11. The college representative/cover slide is already handled separately. An optional application-screens/demo slide and final Thank You slide can be added separately.

## Contents
1. Introduction
2. Problem Statement
3. Existing Systems
4. Proposed System
5. System Architecture
6. Technologies Used
7. Key Features
8. Working / Application Flow
9. Advantages
10. Future Scope
11. Conclusion
12. Application Screens / Demo (Optional)
13. Thank You

---

## 1. Introduction

### Purpose
Introduce TermRunway as a student-focused money planner and expense tracker before discussing the problem.

### Main message
**TermRunway is a student-focused money planner and expense tracker designed to help students understand their actual spending and manage it according to their planned budget.**

### Points
- Tracks daily income and expenses with simple user input.
- Helps students compare actual spending with their planned budget.
- Uses the student's plan to calculate relevant budget information over the planned period.
- Provides simple spending guidance to help students understand whether spending is within or beyond their plan.
- Designed to reduce unnecessary manual effort while maintaining a clear view of student finances.

### Visual direction
- Heading/bar: **1. INTRODUCTION**
- Left: short introduction text.
- Right: TermRunway logo/product visual.
- Use selected real application screens below/after the introduction rather than turning the slide into a feature gallery.

---

## 2. Problem Statement

### Purpose
Explain the actual student money-management problem that motivated TermRunway.

### Main problem
Students may know how much money they have and may record individual expenses, but they often lack a simple way to understand whether their actual spending is appropriate for their planned budget over time.

### Points
- Students often track individual expenses without understanding their impact on the overall budget.
- Daily spending can vary significantly, making it difficult to judge whether spending is sustainable throughout the planned period.
- Manual methods such as notebooks require continuous recording, calculation, and comparison.
- UPI/transaction applications primarily provide transaction records rather than the specific student-oriented planning workflow addressed by TermRunway.
- Students need a simple system that connects **actual spending with planned spending**.

### Example
If a student plans ₹10,000 for a month but spends ₹5,000 today, a transaction history can show the ₹5,000 expense. The student also needs to understand how that spending affects the overall plan and the remaining period.

### Visual direction
- Left: problem statement text.
- Right: Canva illustration showing student + money + notebook/phone + difficulty understanding spending.
- Establish the problem before presenting TermRunway as the solution.

---

## 3. Existing Systems

### Purpose
Show common ways students currently handle money and identify the gap TermRunway addresses.

### Existing approaches

#### UPI / Transaction Apps
**Strength:** Convenient transaction records.  
**Limitation:** Primarily focused on transactions and do not provide the specific student-oriented planning workflow addressed by TermRunway.

#### Notebook / Manual Tracking
**Strength:** Simple and accessible.  
**Limitation:** Requires continuous manual recording, calculation, and comparison.

#### Spreadsheet
**Strength:** Flexible and powerful.  
**Limitation:** Requires additional setup, formulas, and maintenance for everyday student tracking.

### Main conclusion
**Existing methods can record or organize financial information, but connecting everyday spending with a personal plan can require additional effort.**

### Visual direction
Use three comparison cards/columns rather than a long paragraph.

---

## 4. Proposed System

### Purpose
Present TermRunway as the proposed approach to the identified problem.

### Core concept
**One genuine user input → shared financial data → daily tracking + plan tracking → calculations → insights/results**

The student does not need to enter the same expense separately into daily tracking and plan tracking.

### Main message
**TermRunway simplifies student money management by allowing users to record their actual income and expenses once. The system uses this data across daily tracking and planned-budget calculations, reducing duplicate data entry and helping students understand their spending in relation to their plan.**

### Example
If the student records **₹150 — Food**, that transaction can contribute to:
- Daily expense tracking
- Planned-period calculations
- Remaining budget
- Spending comparison
- Insights/guidance

### Important wording
Preferred wording:
**“Once a plan is created, TermRunway uses the user's recorded transactions and the plan's date range to calculate the relevant budget information automatically.”**

### Visual direction
**INPUT ONCE → PROCESS → MULTIPLE INSIGHTS**

---

## 5. System Architecture

### Purpose
Show **how TermRunway runs internally**, not another feature list or problem explanation.

### Architecture concept
**USER → Genuine Income/Expense Input → TermRunway Data → Daily Tracking Engine + Plan Tracking Engine → Calculation & Comparison → Insights/Results**

### Core architectural message
**One user input → shared data → multiple processing paths → combined insights**

### Input
- Income
- Expense
- Category
- Date
- Amount

### Processing
- Daily Tracking
- Plan Tracking
- Budget Calculation
- Spending Comparison

### Output
- Current Spending
- Remaining Budget
- Plan Progress
- Spending Insights

### Bottom statement
**“Enter financial data once — TermRunway processes it across daily tracking and planned budgeting to generate meaningful insights.”**

---

## 6. Technologies Used

### Purpose
Show the complete technology and development environment used to build TermRunway. This is not limited to programming languages.

### Application Development
- Android Studio — primary development environment
- Android / Native Android — application platform
- Java — application programming
- XML — UI/layout development

### Version Control & Project Management
- Git — version control
- GitHub — repository hosting, branch management, phase-based development, documentation, and project history

### AI-Assisted Development
- AI-assisted development workflow — implementation support, analysis, debugging, refinement, and development assistance
- Gemini in Android Studio — AI development assistant used during implementation

### Development & Testing
- Physical Android device + USB debugging — application testing
- GitHub-based documentation — decisions, phases, issues, research, and development handoff

### Main statement
**“TermRunway was developed using a combination of native Android technologies, version control, GitHub-based project management, and an AI-assisted development workflow.”**

### Presentation principle
Do not present GitHub or AI as programming languages. Present them as part of the **engineering/development environment and workflow** used to build and manage the project.

---

## 7. Key Features

### Purpose
Present the product's major capabilities and user value without turning the slide into a list of every screen or button.

### Core features
1. **Daily Income & Expense Tracking** — Record actual income and expenses with amount, category, date, and details.
2. **Student Budget Planning** — Create a financial plan for a defined period based on available money and expected spending.
3. **Unified Daily & Plan Tracking** — The same financial input supports both daily tracking and planned-budget calculations, avoiding duplicate entry.
4. **Budget Progress & Spending Insights** — Understand spending progress, remaining budget, and how actual spending relates to the plan.
5. **Category-Based Expense Management** — Organize spending into categories such as food, transport, study, bills, entertainment, and others.
6. **Activity History** — View and manage previously recorded income and expenses.
7. **Spending Guidance** — Provides simple guidance when spending is relatively high or low compared with the user's plan.
8. **Offline & Privacy-Focused** — Native Android application designed for local/offline financial data use without requiring an online account.

### Recommended visual grouping
**TRACK:** Daily Income & Expenses, Activity History, Category Management

**PLAN:** Student Budget Planning, Plan Progress, Remaining Budget

**UNDERSTAND:** Spending Comparison, Insights, Spending Guidance

**DESIGNED FOR STUDENTS:** Offline, Privacy-focused, Simple data entry

### Feature statement
**“Record once. Track daily. Compare with your plan. Understand your spending.”**

---

## 8. Working / Application Flow

### Purpose
Demonstrate how a student actually uses TermRunway. This should **not** be a screenshot gallery containing every screen.

### Recommended workflow
1. **Create Your Plan** — The student creates a financial plan by defining the available money, plan period, and expected spending.
2. **Record Actual Income / Expense** — Whenever money is received or spent, the student records the actual transaction with the relevant amount and category.
3. **Daily Tracking** — TermRunway organizes recorded transactions to show the student's current financial activity and spending.
4. **Plan Progress** — The same recorded financial data is used to calculate progress against the student's active plan.
5. **Insights** — TermRunway compares actual spending with the planned budget and presents understandable spending information and guidance.
6. **Activity** — The student can review previously recorded transactions and manage their financial history.

### Recommended visual
**CREATE PLAN → RECORD ACTUAL TRANSACTION → DAILY TRACKING → PLAN PROGRESS → INSIGHTS → REVIEW ACTIVITY**

Use only the most important real application screens. The purpose is to demonstrate the **user workflow**, not to prove that every screen exists.

### Presentation explanation
**“The working flow of TermRunway begins with creating a plan. The student then records genuine income and expenses as they occur. TermRunway uses this same data for daily tracking and plan calculations, and finally presents the resulting progress and insights to help the student understand their spending.”**

---

## 9. Advantages

### Purpose
Summarize why the proposed system is useful after explaining its capabilities.

### Points
- **Student-focused:** Designed around student budgeting and spending behavior.
- **One-time data entry:** The same genuine transaction can support multiple tracking and planning calculations.
- **Reduced manual effort:** Minimizes repeated calculations and comparison work.
- **Clear budget understanding:** Connects actual spending with the active financial plan.
- **Simple workflow:** Designed for quick recording and easy interpretation.
- **Offline use:** Financial data can be used locally without requiring an online account.
- **Privacy-focused:** Keeps the product centered on local personal financial data.
- **Practical guidance:** Helps students understand whether spending is relatively high or low against their plan.

### Key statement
**“TermRunway focuses on making student budgeting simpler by connecting actual spending with planned spending in one workflow.”**

---

## 10. Future Scope

### Purpose
Clearly separate planned improvements from the features already implemented.

> **Important:** These are future enhancements, not claims about the current version.

### Potential enhancements
- **Saving Goals** — allow students to define and track specific savings targets.
- **Saving-Goal Reminders / Notifications** — optional reminders related to savings progress.
- **Advanced Spending Analytics** — deeper trends and historical comparisons.
- **Custom Categories** — allow users to create categories suited to their needs.
- **Enhanced Backup / Restore** — improve portability and recovery of local financial data.
- **Optional Cloud Synchronization** — future support for synchronized data across devices.
- **Intelligent Financial Guidance** — more advanced, personalized spending insights.

### Key statement
**“Future development can extend TermRunway from a focused student budget tracker into a broader personal financial planning assistant.”**

---

## 11. Conclusion

### Purpose
Close the presentation by returning to the original problem and showing how TermRunway addresses it.

### Main conclusion
**TermRunway provides a simple student-focused approach to managing money by connecting actual daily transactions with a planned budget. By using shared financial data, the system reduces duplicate entry and helps students understand their spending progress through clear calculations and insights.**

### Short closing points
- Identifies a practical student financial-management problem.
- Connects daily transactions with planned budgeting.
- Uses one genuine input across multiple processing paths.
- Provides understandable spending progress and guidance.
- Designed as an offline, privacy-focused native Android application.

### Final statement
**“Record once. Understand your spending. Manage your plan.”**

---

## 12. Application Screens / Demo — Optional

Use this only if the college presentation benefits from a separate proof-of-application slide.

### Recommended screens
**Home → Plan → Add Transaction → Activity → Insights**

### Purpose
Show selected real application screens as evidence of the implemented product, not as a complete screen gallery.

---

## 13. Thank You

### Suggested slide
**THANK YOU**

**Questions?**

Keep this slide visually simple.

---

# Final Presentation Story

The complete narrative is:

**Introduction → Problem Statement → Existing Systems → Proposed System → System Architecture → Technologies Used → Key Features → Working/Application Flow → Advantages → Future Scope → Conclusion**

The story should feel like:

**Problem → Gap → Solution → Internal Processing → Engineering → Capabilities → Real Usage → Value → Future → Conclusion**

The presentation should consistently distinguish:
- **Current implemented functionality** from **future scope**
- **Technologies used** from **development tools/workflow**
- **Architecture** from **feature lists**
- **Working flow** from a generic screenshot gallery

The goal is to make the PPT explain **why TermRunway exists, what problem it addresses, how it works, how it was built, what it currently provides, and where it can go next**.
