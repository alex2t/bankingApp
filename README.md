# ATM-osphere (bankingApp)

A demo Android banking app written in Kotlin with Jetpack Compose. It stores data
locally in SQLite and reports user activity (login, add payee, payment) to a remote
listener over HTTPS.

## Background

This project was developed as a mobile app to test a solution at my work, and to learn
Kotlin. It was my first "vibe coding" project: ChatGPT was used at first, then Claude.
At the time the workflow was copying and pasting files into the AI to get a fix; it
usually returned a page with the fix and a lot of new errors. AI back then was not what
it is now.

## Timeline

- First commit: 2024-10-22
- Last commit: 2025-02-07 (26 commits)

| Period | Work |
|---|---|
| Oct 2024 | Project setup, ViewModel factories, logout fix, file structure |
| Nov 2024 | Add payee and pay payee, IBAN generator, relational database (SQLCipher tried, then switched to plain SQLite), logout on background and after 5 minutes of inactivity, first POST to the listener (login) |
| Dec 2024 | POST for add payee and transactions, swipe-to-delete transactions, delete payee |
| Jan-Feb 2025 | UI redesign and responsive layout, Firebase Analytics, Performance and Crashlytics, screen rotation fix |

## Features

### Register and login
- Home page shows the logo, a generated session ID, and Sign In / Register buttons.
- Register creates a user (email + password) with a random UUID as the permanent user
  ID (PUID).
- Sign in checks email and password against the local database, then navigates to the
  account page.
- The session ID and PUID are displayed at the bottom of each screen.

### Pre-existing payees and transactions
On account creation (not at build time), default data is inserted for the new user:
- 8 payees: Netflix, Salary, John Doe, Gym, Sponsor (US, hidden from payee lists) and
  Frederick Schmidt (DE), Peter Hendrik (NL), Pat Murphy (IE) (visible and payable).
- 5 transactions dated January 2024 against the hidden payees (debits and credits).

### Account statement (View Transactions)
- Lists the user's transactions as cards: payee name, type (credit in green, debit in
  red), date and amount, newest first.
- Swipe a row left to delete it. A snackbar offers Undo to restore it.
- There is no account balance or bank card feature; "card" in the UI refers to the
  Material card components used for layout.

### Manage payees
- Add Payee tab: enter a name (letters only, max 20 chars), pick a country (Israel, UK,
  France, Spain, USA, Japan) and generate a fake IBAN with valid check digits, then add.
- Delete Payee tab: list of payees with a Delete button on each row.

### Make payment
- Select a payee, enter an amount (up to 6 digits, 2 decimals) and pay. The payment is
  stored as a debit and appears in the statement.

### Session security
- Automatic logout after 5 minutes of inactivity.
- Logout when the app goes to the background, or on back press from a logged-in page.

### Communication with the server
Login, add payee and payment events are sent as JSON POST requests to
`https://catcheroo.com/listener`, a listener hosted on my VPS (still running). Its
source is at https://github.com/alex2t/catcheroo.

Each payload carries the `activity` (`Login`, `AddPayee`, `Transaction`), session ID,
PUID, user agent and public IP (looked up via api64.ipify.org), plus the payee data or
payee IBAN, country, amount and type. Deletes are not sent to the server.

## Test app, not production

This is a test app, used only by me, so some choices are deliberate:

- Passwords are stored in plain text in the local database.
- The database was first encrypted with SQLCipher, then switched to plain SQLite so it
  could be inspected and edited (CRUD) directly from Android Studio's Database Inspector.
- The listener has no authentication. Received posts are only shown on its web page and
  disappear after 5 minutes.
- Collecting the user agent and public IP was part of the test.

## Tech stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose, MVVM with ViewModels
- SQLite (`SQLiteOpenHelper`) with WorkManager for background inserts
- OkHttp and kotlinx.serialization for the API calls
- Firebase Analytics, Performance Monitoring and Crashlytics
- LeakCanary (debug builds)
- minSdk 24, targetSdk 34, compileSdk 35, AGP 8.7.3, Gradle 8.9

## Project structure

```
app/src/main/java/com/example/atmosphere/
  MainActivity.kt, InactivityManager.kt, InactivityLifecycleObserver.kt
  model/        data classes and API payloads
  utils/        API client, database helpers, workers, IBAN generator
  view/         Compose screens (auth, navigation, postLogin)
  viewmodels/   auth, payee and transaction ViewModels
```

## Build and run

Open the project in Android Studio and run the `app` configuration, or:

```
./gradlew installDebug
```

The app requests location permission on start; location is not used otherwise.
