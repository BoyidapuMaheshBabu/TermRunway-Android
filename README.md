# TermRunway 1.0.0

Your money. Your semester. Your runway.

TermRunway is an offline-first native Android student-finance application with two core modes.

Daily Tracking answers "What happened?"
- Record income and expenses.
- Review daily, weekly and monthly activity.
- Search and filter transactions.
- View spending by category.
- View a simple spending trend.

Plan Tracking answers "What will happen?"
- Select a custom plan period.
- Record money already available at the start.
- Add expected income.
- Add planned expenses.
- See expected remaining money while planning.
- Compare actual plan-period activity against the plan.
- Calculate remaining money per day.
- Show contextual spending-pace guidance.

Architecture:
Jetpack Compose -> ViewModel -> FinancialCalculator / Repository -> Local SQLite
Small preferences -> DataStore
Backup/restore -> Android Storage Access Framework + JSON

The product is deliberately offline. It requests only the user's name and does not require an account, backend, bank connection, cloud sync, internet permission or financial API.

Financial data uses integer paise internally to avoid floating-point currency errors.

The branch is a clean rebuild based on the full TermRunway product definition rather than a comparison with the previous screen implementation.

AI is used as a development accelerator and learning assistant. Product decisions, testing and final engineering judgment remain part of the project workflow.

GitHub Actions validates the debug build and unit tests on the termrunway branch.

Developer: Boyidapu Mahesh Babu
