# Architecture

Static description of the Housiness Android app. No build or device run was
performed.

## Layers

```
MainActivity
  └─ AppNavigation (Jetpack Compose, Navigation Compose)
       ├─ Home      → HomeViewModel
       ├─ Products  → ProductViewModel
       ├─ Orders    → OrderViewModel
       └─ More      → SettingsScreen (local UI state)
HousinessApp
  └─ ServiceLocator → AppDatabase, 8 DAOs, 6 repositories
```

- `HousinessApp` is the `Application` class and builds a manual `ServiceLocator`;
  there is no Hilt or Koin.
- `MainActivity` is the only activity. It enables edge-to-edge and hosts
  `AppNavigation`.
- `ServiceLocator` owns the singleton Room database, 8 DAOs and 6 repositories
  (Product, Pantry, Order, Invoice, MessageTemplate, Settings).

Navigation has four top-level tabs: Home (financial summary), Products, Orders and
More (settings hub). First run is gated behind a setup screen until a business name
is saved.

Detail screens are not navigation destinations. `SettingsScreen` renders invoice
history, invoice preview, templates, backup, pantry and name editing through local
boolean state. Product and order detail flows use their own ViewModel state
(`showNewOrderScreen`, `showDetailSheet`).

## Data model

Room database `housiness.db`, version 2, `exportSchema = false`, 8 entities and 8
DAOs with 63 Room annotations.

| Table | Notable columns |
| --- | --- |
| `products` | name, selling price, making cost, cost source (`MANUAL`/`CALCULATED`), createdAt — money stored as `Long` |
| `product_materials` | product id, optional pantry material id, name, unit type (`KG`/`G`/`LITER`/`ML`/`UNIT`), unit cost, price paid, unit size, quantity used, calculated cost |
| `pantry_items` | ingredient records |
| `orders` | product snapshot (id + name), quantity, total price, total cost, status (`UPCOMING`/`COMPLETED`), dateTime, optional invoiceId |
| `invoices` | orderId, `order_ref` (`INV-###`), optional customer name, subtotal, generatedAt |
| `invoice_line_items` | invoice id, product name, quantity, unit price, total |
| `message_templates` | title, body, lastEditedAt |
| `app_settings` | single row (`id = 1`) holding the business name |

One migration exists: 1→2 adds `app_settings` and inserts the default business-name
row. There are no declared Room foreign keys; referential consistency is enforced
by app code.

## Invoice flow

1. `OrderViewModel` generates an invoice for an order.
2. `InvoiceRepository` numbers it from the lexicographically last `order_ref` as
   `INV-###`.
3. Line items are written with unit price `totalPrice / quantity` (quantity
   clamped to at least 1).
4. The invoice id is attached to the order. The order is **not** marked complete by
   this step.
5. `PdfGenerator` draws a single A4-point page (595×842) with
   `android.graphics.pdf.PdfDocument` and the business name.
6. The file is shared through `FileProvider` (authority
   `${applicationId}.fileprovider`).

## Backup flow

`BackupManager` writes JSON with:

- a `version` field (currently 2),
- an export timestamp,
- every table's rows, including settings.

File name pattern: `housiness-backup-YYYY-MM-DD.json`. Import supports merge and
replace modes.

## Configuration

No environment variables, no `BuildConfig` fields and no configuration file. The
only user setting is the business name in `app_settings`.

## Build, tests and CI

- AGP 8.2.2, Kotlin 1.9.22, KSP 1.9.22-1.0.17, Gradle wrapper 8.5, JVM 17.
- Compose BOM 2024.02.00, Material 3, Navigation Compose 2.7.6, Room 2.6.1,
  Gson 2.10.1.
- Dependency versions are hardcoded in `app/build.gradle.kts`; there is no version
  catalog.
- `app/src/test` is empty and no test dependencies are declared, so
  `testDebugUnitTest` runs zero tests.
- `ci.yml`: lint, `testDebugUnitTest`, `assembleDebug`, debug APK upload.
- `release.yml`: triggered by `v*` tags, builds the debug APK and attaches it to a
  GitHub release.

## Unknowns

- Whether the app has been exercised on a device.
- Whether a published APK exists.
- Whether the empty `app/src/test` directory is intentional.
