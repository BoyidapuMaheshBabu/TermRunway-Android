# TermRunway Android

TermRunway is a native Android student-finance app built with Kotlin and Jetpack Compose.

## Current product scope

The active product is Daily Tracking:

- Home: today's spending, income, net, recent activity, quick actions.
- Track: day-by-day navigation, calendar selection, add/edit/delete transactions.
- Insights: 7/30/90-day spending and income summaries with category breakdown.
- Settings: profile name, informational daily limit, theme, backup/restore, local data reset.
- Storage: local JSON on the device. No account or server is required.

Term Mode is intentionally not active in this rebuild. It will be designed after Daily Tracking is stable.

## Architecture

- data/: models and local persistence/backup format.
- domain/: pure money and financial calculations.
- ui/components/: reusable finance UI and the transaction editor.
- ui/screens/: Home, Track, Insights, Settings, and first-run setup.
- ui/navigation/: bottom navigation.
- ui/theme/: visual system.
- util/: date and money formatting.

The screen layer owns UI state and persistence goes through TermRunwayRepository. The repository validates data before writing and fully parses a backup before a restore can replace local data.

## Development rule

Build -> Problem -> Explore -> Fix -> Improve -> Test

Do not add Term Mode until the Daily Tracking loop is reliable on a physical device.
