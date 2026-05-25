# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

JavaFX desktop application (`org.itss.prj_itss`) managing the import-ordering workflow: sales requests → international ordering / allocation → warehouse receiving. Vietnamese-language UI. See `BussinessLogic.md` for the full domain spec (actors: Sales, International Ordering, Site, Admin, Warehouse).

Java 17, JavaFX 17, PostgreSQL (Supabase). Build via Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Common commands

```bash
# Run the app (JavaFX)
./mvnw clean javafx:run        # macOS/Linux
.\mvnw.cmd clean javafx:run    # Windows

# Compile only
./mvnw -DskipTests compile

# Run all tests
./mvnw test

# Run a single test class / method
./mvnw test -Dtest=MvcDependencyTest
./mvnw test -Dtest=AllocationPolicyTest#methodName
```

Main class is wired through `javafx-maven-plugin` in `pom.xml`: `org.itss.prj_itss/org.itss.prj_itss.App`.

## Architecture

Strict MVC layering enforced by `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java`. Violating these rules will break the build:

- `model.*` must not import `view.*`, `controller.*`, or `javafx.*`
- `controller.*` must not import `view.*` or `javafx.*`
- `view.*` must not import any `*.persistence.*` / `Jdbc*` / `model.shared.database.*`

Top-level packages under `org.itss.prj_itss`:

- `App.java` - JavaFX entrypoint. Owns the `Stage`, swaps scenes between login and main layout, runs DB warm-up off the FX thread.
- `bootstrap/` - composition root. `MvcContext` wires database providers, model modules, flow controllers, and `bootstrap.navigation.RouteRegistry` (manual DI, no framework). `ViewLoader` loads the login/main FXML shells and passes only route/session callbacks into views. **All new use cases, controllers, or routes must be registered here**, not constructed ad-hoc inside views.
- `model/` - business logic, organized per bounded context (`auth`, `catalog`, `order`, `request`, `site`, `warehouse`, `shared`). Each context follows: `domain/` (entities + value objects), `application/` (use cases, ports, application services), `infrastructure/persistence/` (JDBC adapters implementing ports).
- `model/shared/database/` - database support shared by JDBC adapters: `ConnectionProvider`, `JdbcRepositorySupport`, connection providers, transaction managers, and the `TransactionRunner` port.
- `model/shared/formatting/` - shared display/key formatting used by model read models and views.
- `controller/` - flow controllers grouped by area (`auth`, `home`, `ordering`, `sales`, `warehouse`, `navigation`, `shared`). They orchestrate use cases from `model` and call the navigator to switch screens. No JavaFX types here.
- `view/` - JavaFX FXML controllers loaded by `FXMLLoader`. They hold UI state and forward user actions to a flow controller injected via `setController(...)`. `view/shared/ui/` holds reusable JavaFX UI helpers. Each FXML-bound view package is opened to `javafx.fxml` in `module-info.java`.
- `dashboard/application/` - read-side query (`DashboardQuery`) aggregating across request/order/site use cases.

FXML, CSS, and images live under `src/main/resources/org/itss/prj_itss/`, mirroring the view package structure.

## Conventions

- **Wiring**: add new modules, repositories, use cases, flow controllers, or routes in `MvcContext`. Add screens through `RouteRegistry` route objects instead of editing `MainLayoutView`. Don't `new` services from inside views or controllers.
- **Module system**: this is a JPMS module (`module org.itss.prj_itss`). New FXML-bound view packages need an `opens` directive in `module-info.java`.
- **Transactions**: use `TransactionRunner`/`TransactionManager` (or the warehouse equivalent) instead of calling `Connection.setAutoCommit`/`commit` directly. Repositories obtain connections through `ConnectionProvider` so the same connection is reused inside a transaction.
- **Cross-database operations**: warehouse receiving uses `WarehouseReceivingUseCase`, which composes use cases from the main DB (`orderUseCase`, `siteUseCase`, `catalogUseCase`) with the warehouse repository and a `WarehouseTransactionManager`-backed runner. Don't share the two `TransactionManager`s.

## CodeGraph MCP

This repo has a CodeGraph MCP index (`.codegraph/`). For structural lookups — *where is X defined, what calls Y, what would break if Z changes, show me Y's signature* — prefer `codegraph_*` tools over `Grep`/`Read`. Use grep only for literal text (string contents, log messages). See `.cursor/rules/codegraph.mdc` for the full tool table.

## Database config

- `src/main/resources/db.properties` is checked in with Supabase credentials for the main DB.
- Warehouse DB has no checked-in config — supply `WAREHOUSE_DB_HOST`, `WAREHOUSE_DB_PORT`, `WAREHOUSE_DB_NAME`, `WAREHOUSE_DB_USER`, `WAREHOUSE_DB_PASSWORD` or create `src/main/resources/warehouse-db.properties` locally.
- SQL migrations live in `supabase/migrations/`.
