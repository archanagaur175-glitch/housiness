# Housiness

A local Android app for running a home food or snack business: products, ingredient
costs, orders, invoices, message templates and a one-page dashboard.

The app keeps everything in a local Room/SQLite database. It has no accounts, no
cloud sync and no analytics.

## Features

- **Home dashboard** — revenue, profit, order count, average profit and margin,
  derived from reactive database queries.
- **Products** — selling price, making cost and a cost source of `MANUAL` or
  `CALCULATED`, with per-material costs.
- **Pantry** — ingredient records with unit types KG, G, LITER, ML and UNIT.
- **Orders** — product snapshot, quantity, total price, total cost, status
  (`UPCOMING` or `COMPLETED`) and an optional invoice reference.
- **Invoices** — sequential `INV-###` numbers rendered to a one-page PDF with the
  business name.
- **Message templates** — reusable text with a title and last-edited timestamp.
- **Backup and restore** — the whole database exports to JSON and imports with
  merge or replace modes.
- **First-run setup** — the app stays on a setup screen until a business name is
  saved.

## Requirements

- Android 8.0 (API 26) or newer; `compileSdk` and `targetSdk` are 34.
- JDK 17.
- Android SDK 34.
- The Gradle wrapper is committed (Gradle 8.5), so no local Gradle install is
  needed.

## Build and run

```powershell
.\gradlew.bat assembleDebug
```

Install the resulting APK from `app/build/outputs/apk/debug/` with your usual
`adb install` or device-side installer. Release builds are not minified, and no
release signing configuration is present in the repository.

## Data and backup

The Room database is `housiness.db`, version 2, with eight tables:
`products`, `product_materials`, `pantry_items`, `orders`, `invoices`,
`invoice_line_items`, `message_templates` and `app_settings`.

Only one migration exists: 1→2 adds `app_settings` and inserts the default
business-name row. `exportSchema = false`, so schema history is not checked in.

Backups are JSON files named `housiness-backup-YYYY-MM-DD.json`, carrying a
`version` (currently 2), an export timestamp and every table's rows, including
settings. Import offers merge and replace modes.

## PDF invoices

Invoices render with the platform `android.graphics.pdf.PdfDocument` on a single
A4-point page (595×842). No third-party PDF library is used. The PDF is shared
through a `FileProvider` with authority `${applicationId}.fileprovider`.

Generating an invoice attaches its id to the order but does not mark the order
complete; completion is a separate action. Line-item unit price is
`totalPrice / quantity` with quantity clamped to at least 1.

## Permissions

The manifest declares one permission, `INTERNET`. No networking code is present,
so the permission is not currently used. There are no runtime permissions.

## Architecture

`HousinessApp` creates a manual `ServiceLocator` (no Hilt or Koin) that owns the
singleton database, 8 DAOs and 6 repositories. `MainActivity` is the only activity
and hosts `AppNavigation`, which renders four tabs: Home, Products, Orders and
More. Sub-screens such as invoice history, invoice preview, templates, backup and
pantry are local UI states inside `SettingsScreen` rather than navigation
destinations.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the entity/DAO/repository
map and the invoice and backup flows.

## Tests and CI

`app/src/test` exists but is empty, and no test dependencies are declared, so
`testDebugUnitTest` runs zero tests. No instrumented tests exist.

CI (`.github/workflows/ci.yml`) runs on push and pull request to `main` (ignoring
markdown-only changes) and on manual dispatch: lint, `testDebugUnitTest`,
`assembleDebug`, and a debug APK upload.

`release.yml` triggers on `v*` tags, **builds the debug APK** and attaches it to a
GitHub release with generated notes. It is not a signed production build.

## Known limitations

- No test coverage; the CI test step is a no-op that still reports success.
- Release artifacts are debug-signed.
- Currency formatting uses the device default locale with zero fraction digits,
  so amounts may not show ₹ on a non-Indian device.
- Invoice numbering derives from the lexicographically last `order_ref`, which
  depends on zero-padding staying at three digits.
- All user-facing text is hardcoded in Compose; `strings.xml` contains only
  `app_name`.
- The `INTERNET` permission is declared but unused.

## License

MIT. See [`LICENSE`](LICENSE).
