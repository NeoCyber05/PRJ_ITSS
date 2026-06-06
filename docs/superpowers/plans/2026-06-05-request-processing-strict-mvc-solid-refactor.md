# Request Processing Strict MVC/SOLID Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the request-processing and cancellation-processing screens so the `view` layer contains only JavaFX/FXML UI classes, while controller-owned presentation contracts and model-side business rules are moved to their proper MVC layers.

**Architecture:** Keep strict MVC vocabulary. Model owns domain data, validation, allocation and suggestion rules. Controller coordinates screen flow and prepares presentation data. View only renders JavaFX/FXML UI and forwards UI events to controllers.

**Tech Stack:** Java 21, JavaFX/FXML, JUnit 5, Maven wrapper, existing `MvcDependencyTest`.

---

## Current Problem

The following packages are not strict-MVC clean because they place non-UI state and business-ish logic under `view`:

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/state`
- `src/main/java/org/itss/prj_itss/view/ordering/order/cancellation/state`

Concrete smells:

- `SiteFilterModel` is UI state, not an MVC Model, and it lives in `view`.
- `RequestProcessingViewModel`, `ProcessingSiteView`, `SuggestedPlanView`, etc. are presentation data contracts, not JavaFX UI.
- `RequestProcessingSession` builds text, CSS class names, and view models, so it has mixed controller-flow and presentation-mapping responsibilities.
- `CancelledOrderProcessingSession` and `OrderCancellationSuggester` live under `view`, but they load processing data and implement allocation suggestion decisions.

## Target Folder Shape

Keep JavaFX classes under `view`:

```text
src/main/java/org/itss/prj_itss/view/ordering/request/process/
  items/
  layout/
  preview/
  shared/
  site/
  suggest/

src/main/java/org/itss/prj_itss/view/ordering/order/
  OrderCancellationView.java
```

Move presentation contracts and mappers under controller:

```text
src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/
  AllocationChangeCommand.java
  AllocationChangeResultView.java
  ProcessingItemView.java
  ProcessingPreviewOrderView.java
  ProcessingSiteView.java
  RequestProcessingPresentationMapper.java
  RequestProcessingSnapshot.java
  RequestProcessingViewModel.java
  SuggestedPlanView.java

src/main/java/org/itss/prj_itss/controller/ordering/request/process/site/
  SiteFilterController.java
  SiteFilterState.java
```

Move cancellation flow state out of view:

```text
src/main/java/org/itss/prj_itss/controller/ordering/order/cancellation/session/
  CancelledOrderProcessingSession.java

src/main/java/org/itss/prj_itss/controller/ordering/order/cancellation/presentation/
  CancelledOrderProcessingViewModel.java
  CancelledOrderPresentationMapper.java

src/main/java/org/itss/prj_itss/model/order/domain/cancellation/
  OrderCancellationSuggester.java
```

No `module-info.java` change should be needed unless an FXML controller class is moved. This plan does not move FXML controller packages.

## Acceptance Criteria

- `view/ordering/request/process/state` no longer exists.
- `view/ordering/order/cancellation/state` no longer exists.
- No `src/main/java/org/itss/prj_itss/view/**/state/*.java` files remain.
- No controller/session class imports `org.itss.prj_itss.view.*.state`.
- `RequestProcessingSession` no longer creates `RequestProcessingViewModel` directly.
- `OrderCancellationSuggester` no longer lives under `view`.
- JavaFX views still compile and render from the same FXML files.
- `MvcDependencyTest` includes a guardrail for `view/**/state`.

---

### Task 1: Add The Failing Strict-MVC Guardrail

**Files:**
- Modify: `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java`

- [ ] **Step 1: Add a failing test that bans state packages under view**

Add this test inside `MvcDependencyTest`:

```java
@Test
void viewLayerDoesNotContainStatePackages() throws IOException {
    List<String> violations = new ArrayList<>();
    for (SourceFile sourceFile : sourceFiles()) {
        String packageName = sourceFile.packageName();
        if (packageName.startsWith(BASE_PACKAGE + ".view.") && packageName.contains(".state")) {
            violations.add(sourceFile.relativePath() + " is non-UI state under View: " + packageName);
        }
    }
    assertNoViolations(violations);
}
```

- [ ] **Step 2: Run the architecture test and confirm it fails for the current code**

Run:

```powershell
.\mvnw.cmd -q test -Dtest=MvcDependencyTest
```

Expected: FAIL listing files under:

```text
view/ordering/request/process/state
view/ordering/order/cancellation/state
```

- [ ] **Step 3: Do not weaken existing MVC tests**

Keep the existing checks:

- `modelDoesNotDependOnViewOrControllerOrJavaFx`
- `controllerDoesNotDependOnJavaFx`
- `viewDoesNotDependOnPersistence`
- FXML path/resource tests

---

### Task 2: Move Request-Processing Presentation Contracts Out Of View

**Files:**
- Move from: `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/*.java`
- Move to: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/*.java`
- Modify imports in:
  - `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`
  - `src/main/java/org/itss/prj_itss/controller/ordering/request/process/session/RequestProcessingSession.java`
  - `src/main/java/org/itss/prj_itss/controller/ordering/request/process/preview/RequestProcessingPreviewDialogController.java`
  - `src/main/java/org/itss/prj_itss/view/ordering/request/process/**/*.java`
  - `src/main/java/org/itss/prj_itss/view/ordering/order/OrderCancellationView.java`
  - `src/main/java/org/itss/prj_itss/view/ordering/order/cancellation/state/*.java`
  - `src/test/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutControllerTest.java`

- [ ] **Step 1: Move the presentation records and change their package**

Each moved file should start with:

```java
package org.itss.prj_itss.controller.ordering.request.process.presentation;
```

Move these records first without changing field names:

```text
AllocationChangeCommand.java
AllocationChangeResultView.java
ProcessingItemView.java
ProcessingPreviewOrderView.java
ProcessingSiteView.java
RequestProcessingViewModel.java
SuggestedPlanView.java
```

Do not move `SiteFilterModel` in this task. It is handled separately as `SiteFilterState`.

- [ ] **Step 2: Replace imports mechanically**

Old import prefix:

```java
org.itss.prj_itss.view.ordering.request.process.state
```

New import prefix:

```java
org.itss.prj_itss.controller.ordering.request.process.presentation
```

- [ ] **Step 3: Compile after the package move**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: PASS. If it fails, fix only imports/package declarations in this task.

- [ ] **Step 4: Verify old package imports are gone**

Run:

```powershell
rg -n "view\.ordering\.request\.process\.state" src/main/java src/test/java
```

Expected: no matches except files not yet moved in Task 3 if the search still sees `SiteFilterModel`.

---

### Task 3: Rename SiteFilterModel To Controller-Owned SiteFilterState

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/site/SiteFilterState.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/site/SiteFilterController.java`
- Delete after migration: `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/SiteFilterModel.java`

- [ ] **Step 1: Create `SiteFilterState` in the controller package**

Use the existing `SiteFilterModel` body, but change the class/package:

```java
package org.itss.prj_itss.controller.ordering.request.process.site;

import org.itss.prj_itss.controller.ordering.request.process.presentation.ProcessingSiteView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class SiteFilterState {
    // Copy the existing fields and methods from SiteFilterModel.
    // Keep the class package-private unless tests require public access.
}
```

Concrete rule: keep method names the same as the current `SiteFilterModel`, so `SiteFilterController` stays a thin delegator:

```java
setSites(...)
allSites()
visibleSites()
selectedSiteIds()
excludedSiteIds()
clearFilters()
select(...)
deselect(...)
exclude(...)
removeSelected(...)
removeExcluded(...)
isSelected(...)
refreshVisibleSites(...)
selectedSites()
excludedSites()
```

- [ ] **Step 2: Update `SiteFilterController`**

Change:

```java
import org.itss.prj_itss.view.ordering.request.process.state.SiteFilterModel;
```

to no import, because `SiteFilterState` is in the same package.

Change the field:

```java
private final SiteFilterState model = new SiteFilterState();
```

- [ ] **Step 3: Update `ProcessingSiteView` imports**

Use:

```java
import org.itss.prj_itss.controller.ordering.request.process.presentation.ProcessingSiteView;
```

in both controller and JavaFX view classes.

- [ ] **Step 4: Run focused compile and MVC test**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd -q test -Dtest=MvcDependencyTest
```

Expected: compile PASS. `MvcDependencyTest` may still fail until Task 4 removes the cancellation `view/.../state` package.

---

### Task 4: Move Cancellation Processing State Out Of View

**Files:**
- Move: `src/main/java/org/itss/prj_itss/view/ordering/order/cancellation/state/CancelledOrderProcessingSession.java`
- Move: `src/main/java/org/itss/prj_itss/view/ordering/order/cancellation/state/CancelledOrderProcessingViewModel.java`
- Move: `src/main/java/org/itss/prj_itss/view/ordering/order/cancellation/state/OrderCancellationSuggester.java`
- Modify:
  - `src/main/java/org/itss/prj_itss/controller/ordering/order/OrderCancellationProcessingController.java`
  - `src/main/java/org/itss/prj_itss/controller/ordering/order/OrderControllerModule.java`
  - `src/main/java/org/itss/prj_itss/view/ordering/order/OrderCancellationView.java`

- [ ] **Step 1: Move `CancelledOrderProcessingSession` to controller session**

New package:

```java
package org.itss.prj_itss.controller.ordering.order.cancellation.session;
```

Update imports in `OrderCancellationProcessingController`, `OrderControllerModule`, and `OrderCancellationView` to:

```java
import org.itss.prj_itss.controller.ordering.order.cancellation.session.CancelledOrderProcessingSession;
```

- [ ] **Step 2: Move `CancelledOrderProcessingViewModel` to controller presentation**

New package:

```java
package org.itss.prj_itss.controller.ordering.order.cancellation.presentation;
```

Update its shared request-processing imports to:

```java
import org.itss.prj_itss.controller.ordering.request.process.presentation.ProcessingItemView;
import org.itss.prj_itss.controller.ordering.request.process.presentation.ProcessingSiteView;
```

Update controller/view imports to:

```java
import org.itss.prj_itss.controller.ordering.order.cancellation.presentation.CancelledOrderProcessingViewModel;
```

- [ ] **Step 3: Move `OrderCancellationSuggester` to model domain**

New package:

```java
package org.itss.prj_itss.model.order.domain.cancellation;
```

Update `CancelledOrderProcessingSession` to import:

```java
import org.itss.prj_itss.model.order.domain.cancellation.OrderCancellationSuggester;
```

Reason: choosing sites and transport is not JavaFX UI. It is a model-side allocation decision.

- [ ] **Step 4: Run the strict state-package search**

Run:

```powershell
rg -n "package org\.itss\.prj_itss\.view\..*\.state|view\.ordering\..*\.state" src/main/java src/test/java
```

Expected: no matches.

- [ ] **Step 5: Run the architecture test**

Run:

```powershell
.\mvnw.cmd -q test -Dtest=MvcDependencyTest
```

Expected: PASS.

---

### Task 5: Extract RequestProcessingPresentationMapper

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingSnapshot.java`
- Create: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingPresentationMapper.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/session/RequestProcessingSession.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`
- Modify test: `src/test/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutControllerTest.java`

- [ ] **Step 1: Add `RequestProcessingSnapshot`**

Create a controller-owned raw screen state record:

```java
package org.itss.prj_itss.controller.ordering.request.process.presentation;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationControl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record RequestProcessingSnapshot(
    int requestId,
    String requestCode,
    LocalDate earliestDeliveryDate,
    int deadlineDays,
    List<ItemRequirement> items,
    List<SiteStockOption> allSites,
    Map<Integer, Map<Integer, Allocation>> allocations,
    Map<Integer, LocalDate> desiredDeliveryDates,
    Set<Integer> excludedSiteIds,
    Set<Integer> selectedSiteIds,
    int expandedItemIndex,
    AllocationControl allocationControl
) {
}
```

- [ ] **Step 2: Add `snapshot()` to `RequestProcessingSession`**

Replace `buildViewModel()` ownership with raw snapshot ownership:

```java
public RequestProcessingSnapshot snapshot() {
    return new RequestProcessingSnapshot(
        requestId,
        requestCode(),
        earliestDeliveryDate,
        deadlineDays,
        List.copyOf(items),
        List.copyOf(allSites),
        copyAllocations(allocations),
        Map.copyOf(desiredDeliveryDates),
        excludedSiteIds(),
        selectedSiteIds(),
        expandedItemIndex,
        allocationControl
    );
}
```

Add a helper that prevents callers from mutating session internals:

```java
private static Map<Integer, Map<Integer, Allocation>> copyAllocations(
    Map<Integer, Map<Integer, Allocation>> source
) {
    Map<Integer, Map<Integer, Allocation>> copy = new LinkedHashMap<>();
    source.forEach((itemId, siteAllocations) -> copy.put(itemId, Map.copyOf(siteAllocations)));
    return copy;
}
```

- [ ] **Step 3: Create `RequestProcessingPresentationMapper`**

Move the content currently inside `RequestProcessingSession.buildViewModel()` into:

```java
package org.itss.prj_itss.controller.ordering.request.process.presentation;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationControl;
import org.itss.prj_itss.model.shared.formatting.DeliveryStatusFormatter;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RequestProcessingPresentationMapper {

    public RequestProcessingViewModel toViewModel(RequestProcessingSnapshot snapshot) {
        // Move the existing buildViewModel loop here.
        // Use snapshot.items(), snapshot.allSites(), snapshot.excludedSiteIds(),
        // snapshot.expandedItemIndex(), and snapshot.allocationControl().
        // Keep all status text and CSS class mapping in this mapper.
    }
}
```

When implementing, do not leave presentation strings or CSS class switches in `RequestProcessingSession`.

- [ ] **Step 4: Update `RequestProcessingLayoutController` to own the mapper**

Add:

```java
private final RequestProcessingPresentationMapper presentationMapper =
    new RequestProcessingPresentationMapper();
```

Change `snapshot()`:

```java
public RequestProcessingViewModel snapshot() {
    return presentationMapper.toViewModel(session.snapshot());
}
```

- [ ] **Step 5: Remove `buildViewModel()` from `RequestProcessingSession`**

After controller tests pass, delete `RequestProcessingSession.buildViewModel()` completely. The session should not return `RequestProcessingViewModel`.

- [ ] **Step 6: Run controller tests**

Run:

```powershell
.\mvnw.cmd -q test -Dtest=RequestProcessingLayoutControllerTest
```

Expected: PASS after imports and assertions are updated to the new presentation package.

---

### Task 6: Move Header/Text Calculations Out Of JavaFX Views

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingViewModel.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingPresentationMapper.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/layout/RequestProcessingLayoutView.java`

- [ ] **Step 1: Add UI-ready header fields to `RequestProcessingViewModel`**

Add these fields to the main record:

```java
String titleText,
String summaryText,
String statusText,
```

Target constructor order:

```java
public record RequestProcessingViewModel(
    int requestId,
    String requestCode,
    String earliestDeliveryDate,
    int deadlineDays,
    String titleText,
    String summaryText,
    String statusText,
    List<ProcessingItemView> items,
    List<ProcessingSiteView> sites,
    Map<Integer, String> desiredDeliveryDates,
    List<AllocationItemViewModel> allocationItems
) {
```

- [ ] **Step 2: Build header text in the mapper**

In `RequestProcessingPresentationMapper.toViewModel(...)`, compute:

```java
int totalQuantity = snapshot.items().stream()
    .mapToInt(item -> item.required)
    .sum();

String earliestDeliveryDateText = OrderingFormatters.formatDate(snapshot.earliestDeliveryDate());
String safeDeliveryDate = earliestDeliveryDateText == null || earliestDeliveryDateText.isBlank()
    ? "N/A"
    : earliestDeliveryDateText;

String titleText = "Yêu cầu " + snapshot.requestCode();
String summaryText = "Ngày cần giao: "
    + safeDeliveryDate
    + "  •  " + snapshot.items().size() + " mặt hàng"
    + "  •  " + totalQuantity + " chiếc";
String statusText = "Chờ xử lý";
```

- [ ] **Step 3: Make `RequestProcessingLayoutView.renderHeader()` UI-only**

Replace stream/math/string assembly in `renderHeader()` with:

```java
private void renderHeader() {
    RequestProcessingViewModel vm = controller.snapshot();
    requestCodeLabel.setText(vm.titleText());
    requestSummaryLabel.setText(vm.summaryText());
    requestStatusLabel.setText(vm.statusText());
}
```

- [ ] **Step 4: Search for presentation logic still inside request-processing JavaFX views**

Run:

```powershell
rg -n "\.stream\(|switch \(|mapToInt|flatMap|allocation-fraction-|Ngày cần giao|Chờ xử lý|Yêu cầu " src/main/java/org/itss/prj_itss/view/ordering/request/process
```

Expected: no matches in JavaFX views except harmless UI iteration over child nodes. Any status text, CSS class mapping, or summary text should be in the mapper.

---

### Task 7: Extract Cancellation Presentation Mapping

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/ordering/order/cancellation/presentation/CancelledOrderPresentationMapper.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/order/cancellation/session/CancelledOrderProcessingSession.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/order/OrderCancellationProcessingController.java`

- [ ] **Step 1: Move `CancelledOrderProcessingSession.buildViewModel()` mapping into a mapper**

Create:

```java
package org.itss.prj_itss.controller.ordering.order.cancellation.presentation;

public final class CancelledOrderPresentationMapper {
    public CancelledOrderProcessingViewModel toViewModel(CancelledOrderProcessingSnapshot snapshot) {
        // Move the existing buildViewModel mapping from CancelledOrderProcessingSession here.
    }
}
```

If the implementation needs raw state, create:

```java
src/main/java/org/itss/prj_itss/controller/ordering/order/cancellation/presentation/CancelledOrderProcessingSnapshot.java
```

with the same pattern as `RequestProcessingSnapshot`.

- [ ] **Step 2: Keep cancellation session as flow state only**

After this task, `CancelledOrderProcessingSession` should still:

- start/reset/load cancellation data
- hold current allocation state
- handle allocation input
- confirm and submit replacement orders

It should not:

- build `CancelledOrderProcessingViewModel`
- map CSS classes
- map display dates
- map JavaFX-ready row text

- [ ] **Step 3: Run compile**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: PASS.

---

### Task 8: Final Verification

**Files:**
- Verify all touched production and test files.

- [ ] **Step 1: Confirm strict view-state cleanup**

Run:

```powershell
rg -n "package org\.itss\.prj_itss\.view\..*\.state|view\.ordering\..*\.state" src/main/java src/test/java
```

Expected: no matches.

- [ ] **Step 2: Confirm request-processing view package contains JavaFX/UI classes only**

Run:

```powershell
Get-ChildItem -Path src\main\java\org\itss\prj_itss\view\ordering\request\process -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
```

Expected: only files whose names are UI-ish, such as:

```text
*View.java
*Dialog.java
*CardView.java
*RowView.java
AllocationViewSupport.java
```

- [ ] **Step 3: Run targeted architecture and controller tests**

Run:

```powershell
.\mvnw.cmd -q test -Dtest=MvcDependencyTest
.\mvnw.cmd -q test -Dtest=RequestProcessingLayoutControllerTest
```

Expected: both PASS.

- [ ] **Step 4: Run compile**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: PASS.

- [ ] **Step 5: Run full test suite if time allows**

Run:

```powershell
.\mvnw.cmd -q test
```

Expected: PASS, or report exact failing test names and surefire messages separately from the MVC refactor.

## Notes For Review

- Do not move presentation records into `model`; they contain display text/CSS concerns and are not domain objects.
- Do not make JavaFX views call model classes directly as part of this refactor.
- Do not broaden the architecture test to ban all current view-to-model imports across the whole repo in this task; that would turn this focused refactor into a repo-wide rewrite.
- Keep this refactor mechanical first: package move, mapper extraction, tests, then optional naming cleanup.
