# SPECS.md

This file provides guidance to AI coding agents when working with code in this repository.

## Project

Enveloppe is a native Android budget management app using the cash stuffing envelope method. Written in Kotlin with Jetpack Compose. Requires JDK 17+, Android SDK API 35, minSdk 26.

## Commands

```bash
# Build
./gradlew assembleDebug      # → app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # → app/build/outputs/apk/release/app-release.apk

# Install
./gradlew installDebug       # build + install via USB (USB debugging required)
adb install app/build/outputs/apk/debug/app-debug.apk  # manual install

# Emulator
adb start-server
~/Android/Sdk/emulator/emulator -avd Pixel9
```

There are no automated tests in this project.

## Architecture

Clean Architecture with three layers under `app/src/main/java/net/zygalio/enveloppe/`:

- **`data/`** — Room database (`AppDatabase`), DAOs (`EnvelopeDao`, `CategoryDao`, `ExpenseDao`), entities, and repositories (`EnvelopeRepository`, `ExpenseRepository`). DAOs use `Flow` for reactive queries.
- **`domain/model/`** — Domain models (`EnvelopeSummary`, `EnvelopeDetail`, `Category`, `Expense`, etc.) derived from DB query results.
- **`ui/`** — Jetpack Compose screens in `ui/screen/`, each with a Hilt-injected ViewModel using `StateFlow` + `collectAsStateWithLifecycle()`. Reusable components in `ui/component/`.
- **`di/`** — Hilt modules wiring the DI graph.

### Navigation

Single-activity app (`MainActivity`) with a `NavHost`. Five routes:

| Route                                      | Purpose                         |
| ------------------------------------------ | ------------------------------- |
| `home`                                     | Envelope list                   |
| `envelope_edit?envelopeId={id}`            | Create or edit envelope         |
| `envelope_detail/{envelopeId}`             | Envelope details + expense list |
| `envelope_chart/{envelopeId}`              | Pie chart breakdown by category |
| `expense_edit/{envelopeId}?expenseId={id}` | Create or edit expense          |

### Dependencies

Managed via `gradle/libs.versions.toml`. Key libraries: Compose BOM 2024.12.01, Room 2.6.1, Hilt 2.52, Navigation Compose 2.8.5, Kotlin 2.0.21 with KSP.

## Business Rules

- **No external or non-free APIs**; Google APIs outside AOSP are forbidden.
- Monetary values have no unit; decimals shown only if non-zero (max 2 decimal places).
- `consumed = sum of expenses`; `remaining = budget - consumed`.
- `daily budget = remaining / (days_left + 1)`; not shown if envelope is expired.
- `daily remaining = daily budget - sum of today's expenses`.
- Expense date cannot be in the future or after the envelope's end date.
- A category belongs to a single envelope and cannot be deleted if referenced by any expense.
- Envelope name is required; expense name is required only when no category is assigned.
