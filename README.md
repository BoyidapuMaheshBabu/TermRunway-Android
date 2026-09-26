# TermRunway Android

TermRunway is a native Android student-finance app built with Kotlin and Jetpack Compose.

## Product direction on this branch

TermRunway 1.0.0 is a completely offline app. It asks for only the user's name and stores all financial data locally on the device.

There are two modes:

- **Daily Tracking**: record actual income and expenses without requiring a budget or plan. Use history and insights to understand what happened.
- **Plan Tracking**: define a date range, money available now, expected income, and planned expenses. TermRunway calculates the duration, live totals, plan health, runway, and plan-versus-actual results.

Both modes share the same transaction data. Plan Tracking never creates fake transactions and does not require an account, server, or internet connection.

## UI direction

The branch uses a dark, data-focused native Android visual system with blue primary actions, green positive signals, red warnings, compact rounded cards, a mode switcher on Home, and mode-specific bottom navigation.

## Architecture

- data/: models and local persistence/backup format.
- domain/: pure money, tracking, and term calculations.
- ui/components/: reusable finance UI and the mode switcher.
- ui/screens/: Daily Tracking and Plan Tracking screens.
- ui/navigation/: separate primary navigation for each mode.
- ui/theme/: visual system.

The screen layer owns UI state and persistence goes through TermRunwayRepository. No network service is required for the core app.

## Development rule

Build -> Problem -> Explore -> Fix -> Improve -> Test
