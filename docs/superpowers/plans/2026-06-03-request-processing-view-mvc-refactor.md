# Request Processing View MVC Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the request-processing use case so JavaFX View classes only render UI and forward user events, while Controller classes only coordinate between View, Model, navigation, and presentation mapping.

**Architecture:** The Controller must not contain business logic or presentation calculations. Business rules and allocation state stay in the Model/Application/Domain layer. UI-ready labels, badge text, style class names, warning messages, and summary strings are produced by a dedicated presentation mapper/presenter, then passed to the View through ViewModel records.

**Tech Stack:** Java 21, JavaFX/FXML, Maven, JUnit 5, existing MVC architecture test `MvcDependencyTest`.

---

## MVC Boundary Decision

This plan follows the MVC definition:

> The Controller acts as the middleman between the Model and the View. When a user interacts with the app, the Controller intercepts the request, tells the Model to update or fetch data, takes that data, and passes it to the appropriate View to be rendered.

Therefore:

- **View:** only listens to JavaFX events and renders already-prepared values with `setText`, `setVisible`, `setManaged`, and style-class binding.
- **Controller:** intercepts events, calls use cases/session operations, asks a presenter/mapper to convert state to ViewModel, and performs navigation through `Navigator`.
- **Model/Application/Domain:** owns business rules such as validation, allocation state, stock limits, delivery availability, preview order creation, and submission.
- **Presentation Mapper/Presenter:** owns UI-facing presentation logic such as Vietnamese display text, badge labels, CSS style class selection, fallback labels, and header/preview summaries.

The Controller class itself must not compute totals, switch CSS classes, map error text, or build route strings inline.

## Out Of Scope

- Do not modify `src/main/java/org/itss/prj_itss/view/ordering/request/ReceivedRequestsView.java`.
- Do not refactor the received-requests table pagination/filtering in this plan.
- Do not move domain/business rules into Controller or View.

## File Structure

### Create

- `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingPresentationMapper.java`
  - Converts raw request-processing state/result data into UI-ready ViewModel records.
  - Owns labels, summary strings, warning messages, CSS class names, and fallback display text.

- `src/main/java/org/itss/prj_itss/controller/navigation/OrderingRoutes.java`
  - Centralizes route IDs used by ordering/request-processing controllers.
  - Prevents View classes from knowing `"orders"` or `"received-requests"`.

### Modify

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/RequestProcessingViewModel.java`
  - Add UI-ready fields used by View classes.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/AllocationChangeResultView.java`
  - Add UI-ready `warningMessage`.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/ProcessingPreviewOrderView.java`
  - Add UI-ready preview labels/badges, or introduce a wrapper `ProcessingPreviewViewModel`.

- `src/main/java/org/itss/prj_itss/controller/ordering/request/process/session/RequestProcessingSession.java`
  - Keep session state and domain/application coordination here.
  - Remove UI text/style mapping from this class where possible.
  - Return raw/domain-derived state to the presentation mapper.

- `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`
  - Inject `Navigator`.
  - Use `RequestProcessingPresentationMapper`.
  - Expose semantic actions: `goBack()`, `openCreatedOrders()`, `previewDialogController(...)`.

- `src/main/java/org/itss/prj_itss/controller/ordering/request/RequestControllerModule.java`
  - Accept `Navigator`.
  - Pass it into `RequestProcessingLayoutController`.

- `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
  - Construct `RequestControllerModule` with `navigator`.
  - Stop passing route callbacks into `RequestProcessingLayoutView`.
  - Continue handling the existing dynamic route prefix for request-processing screens.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/layout/RequestProcessingLayoutView.java`
  - Remove `Consumer<String> navigateToView`.
  - Remove route strings.
  - Remove header total calculation.
  - Call semantic controller methods.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/items/ItemsSectionItemRowView.java`
  - Remove `switch` from Vietnamese status text to CSS class.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/items/AllocationItemEditorView.java`
  - Remove remaining calculation, available-site counting, and stock filtering.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/items/AllocationSiteRowView.java`
  - Remove transport fallback calculation.
  - Remove warning-message switch.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/preview/RequestProcessingPreviewDialogView.java`
  - Remove preview summary stream calculations.

- `src/main/java/org/itss/prj_itss/view/ordering/request/process/preview/PreviewOrderCardView.java`
  - Remove per-order quantity stream calculation.

- `Biểu đồ lớp tổng quát.md`
  - Update class diagram so View no longer appears to own navigation or detailed render logic.

### Test

- `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java`
  - Existing architecture gate. Keep running after each phase.

- Optional targeted tests if implementation time allows:
  - `src/test/java/org/itss/prj_itss/controller/navigation/OrderingRoutesTest.java`
  - `src/test/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingPresentationMapperTest.java`

---

## Phase 0: Baseline

**Files:**

- No code changes.

- [ ] **Step 1: Compile current code**

Run:

```powershell
.\mvnw.cmd -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: Run current MVC architecture test**

Run:

```powershell
.\mvnw.cmd test -Dtest=MvcDependencyTest
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 3: Confirm no accidental scope change**

Run:

```powershell
git diff -- src/main/java/org/itss/prj_itss/view/ordering/request/ReceivedRequestsView.java
```

Expected: no diff.

---

## Phase A: Add Presentation Mapper And UI-Ready ViewModels

**Intent:** Move UI presentation decisions out of View classes without putting them into Controller methods. The Controller delegates to `RequestProcessingPresentationMapper`.

**Files:**

- Create: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/presentation/RequestProcessingPresentationMapper.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/RequestProcessingViewModel.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/AllocationChangeResultView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/state/ProcessingPreviewOrderView.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/session/RequestProcessingSession.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`

- [ ] **Step 1: Add UI-ready fields to `RequestProcessingViewModel`**

Add fields to the outer record:

```java
int totalQuantity,
int itemCount,
String deliveryDateText,
String headerSummaryText,
```

Add fields to `AllocationItemViewModel`:

```java
String allocationStatusStyleClass,
int remaining,
String allocatedBadgeText,
String remainingBadgeText,
String remainingBadgeStyleClass,
int availableSiteCount,
String subtitleText,
List<AllocationSiteRowViewModel> visibleSiteRows,
```

Add field to `AllocationSiteRowViewModel`:

```java
String effectiveTransportLabel
```

- [ ] **Step 2: Add warning text to `AllocationChangeResultView`**

Add:

```java
String warningMessage
```

Keep `errorType` if existing code or tests still need it during transition.

- [ ] **Step 3: Add preview display fields**

In `ProcessingPreviewOrderView`, add:

```java
String orderTitleText,
String siteSummaryText,
String quantityBadgeText,
```

If a dialog-level wrapper is cleaner, create:

```java
public record ProcessingPreviewViewModel(
    String subtitleText,
    List<ProcessingPreviewOrderView> orders
) {
}
```

- [ ] **Step 4: Create `RequestProcessingPresentationMapper`**

Responsibilities:

- Build `headerSummaryText`.
- Format delivery-date fallback such as `N/A`.
- Map allocation state to status text and CSS class.
- Build allocated/remaining badge text.
- Build remaining badge CSS class.
- Filter `visibleSiteRows` for rows with `stock > 0`.
- Build `effectiveTransportLabel`.
- Map allocation input errors to Vietnamese warning text.
- Build preview subtitle and preview order badge text.

Important rule:

- The mapper may compute presentation-derived values.
- The mapper must not validate allocations, mutate allocations, create orders, or decide business outcomes.

- [ ] **Step 5: Keep business state in Model/session, not in View**

In `RequestProcessingSession`, keep or expose raw/domain-derived values:

- item requirements
- site stock options
- current allocations
- allocation item state from `AllocationControl`
- allocation site row state from `AllocationControl`
- delivery status from the existing domain/application flow
- preview orders from `RequestProcessingUseCase`

Remove direct Vietnamese text/CSS-class decisions from `RequestProcessingSession` when the new mapper can own them.

- [ ] **Step 6: Make `RequestProcessingLayoutController` delegate mapping**

Change `snapshot()` so the controller coordinates only:

```java
public RequestProcessingViewModel snapshot() {
    return presentationMapper.toViewModel(session.snapshot());
}
```

If introducing a full raw `session.snapshot()` is too large for one pass, use a temporary method name such as:

```java
public RequestProcessingViewModel snapshot() {
    return presentationMapper.toViewModel(session.buildRawViewState());
}
```

The key rule is that the Controller calls the mapper; it does not calculate totals, labels, CSS classes, or warnings itself.

- [ ] **Step 7: Compile and test**

Run:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test -Dtest=MvcDependencyTest
```

Expected:

```text
BUILD SUCCESS
```

---

## Phase B: Bind View Classes To UI-Ready ViewModel Fields

**Intent:** View classes stop deriving display values from collections, numbers, status text, or error codes.

**Files:**

- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/layout/RequestProcessingLayoutView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/items/ItemsSectionItemRowView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/items/AllocationItemEditorView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/items/AllocationSiteRowView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/preview/RequestProcessingPreviewDialogView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/preview/PreviewOrderCardView.java`

- [ ] **Step 1: Remove header calculation from `RequestProcessingLayoutView`**

Replace stream/sum/summary construction with:

```java
requestSummaryLabel.setText(vm.headerSummaryText());
```

- [ ] **Step 2: Remove status-to-CSS switch from `ItemsSectionItemRowView`**

Replace the switch on `item.allocationStatusText()` with:

```java
setStateClass(allocationFractionLabel, FRACTION_STATE_CLASSES, item.allocationStatusStyleClass());
```

- [ ] **Step 3: Remove badge and site-count calculation from `AllocationItemEditorView`**

Use:

```java
subtitleLabel.setText(item.subtitleText());
allocatedBadgeLabel.setText(item.allocatedBadgeText());
remainingBadgeLabel.setText(item.remainingBadgeText());
setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, item.remainingBadgeStyleClass());
```

Loop over:

```java
item.visibleSiteRows()
```

Do not filter by `stock()` inside the View.

- [ ] **Step 4: Remove transport and warning mapping from `AllocationSiteRowView`**

Use:

```java
transportBox.setValue(siteRow.effectiveTransportLabel());
showWarning(result.warningMessage());
```

Do not switch on `result.errorType()` inside the View.

- [ ] **Step 5: Remove preview stream calculations**

In `RequestProcessingPreviewDialogView`, use the dialog-level preview subtitle generated by the mapper.

In `PreviewOrderCardView`, use:

```java
qtyBadge.setText(order.quantityBadgeText());
```

- [ ] **Step 6: Static check View logic**

Run:

```powershell
rg -n "\.stream\(|switch \(|Math\.abs|mapToInt|flatMap" src/main/java/org/itss/prj_itss/view/ordering/request/process
```

Expected:

- No stream/math/switch results in request-processing View classes for display calculations.
- If results remain in pure UI iteration/loading code, review and document why they are UI-only.

- [ ] **Step 7: Compile and test**

Run:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test -Dtest=MvcDependencyTest
```

Expected:

```text
BUILD SUCCESS
```

---

## Phase C: Stop Creating Controllers Inside Views

**Intent:** A View may create/show another View object such as a dialog, but it must not instantiate Controller classes.

**Files:**

- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/layout/RequestProcessingLayoutView.java`

- [ ] **Step 1: Add preview controller factory method**

In `RequestProcessingLayoutController`, add:

```java
public RequestProcessingPreviewDialogController previewDialogController(
    List<ProcessingPreviewOrderView> previewOrders
) {
    return new RequestProcessingPreviewDialogController(this, previewOrders);
}
```

If Phase A introduced `ProcessingPreviewViewModel`, use that type instead.

- [ ] **Step 2: Replace `new RequestProcessingPreviewDialogController(...)` in View**

In `RequestProcessingLayoutView`, replace controller construction with:

```java
controller.previewDialogController(previewOrders)
```

Remove the direct import:

```java
import org.itss.prj_itss.controller.ordering.request.process.preview.RequestProcessingPreviewDialogController;
```

- [ ] **Step 3: Static check**

Run:

```powershell
rg -n "new .*Controller" src/main/java/org/itss/prj_itss/view/ordering/request/process
```

Expected: no matches.

- [ ] **Step 4: Compile and test**

Run:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test -Dtest=MvcDependencyTest
```

Expected:

```text
BUILD SUCCESS
```

---

## Phase D: Move Navigation Out Of RequestProcessingLayoutView

**Intent:** View classes forward user intent. Controllers perform navigation through `Navigator` and route helpers.

**Files:**

- Create: `src/main/java/org/itss/prj_itss/controller/navigation/OrderingRoutes.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/RequestControllerModule.java`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/layout/RequestProcessingLayoutView.java`

- [ ] **Step 1: Create `OrderingRoutes`**

Add methods:

```java
public final class OrderingRoutes {
    private static final String REQUEST_PROCESSING_PREFIX = "request-processing:";

    private OrderingRoutes() {
    }

    public static String receivedRequests() {
        return "received-requests";
    }

    public static String orders() {
        return "orders";
    }

    public static String requestProcessing(int requestId) {
        return REQUEST_PROCESSING_PREFIX + requestId;
    }

    public static String requestProcessingPrefix() {
        return REQUEST_PROCESSING_PREFIX;
    }
}
```

- [ ] **Step 2: Inject `Navigator` into `RequestProcessingLayoutController`**

Add field:

```java
private final Navigator navigator;
```

Constructor should receive:

```java
RequestProcessingUseCase requestProcessingUseCase,
Navigator navigator,
RequestProcessingPresentationMapper presentationMapper
```

Add semantic navigation methods:

```java
public void goBack() {
    navigator.showView(OrderingRoutes.receivedRequests());
}

public void openCreatedOrders() {
    navigator.showView(OrderingRoutes.orders());
}
```

- [ ] **Step 3: Update `RequestControllerModule`**

Constructor receives `Navigator`.

`requestProcessingLayoutController()` creates:

```java
return new RequestProcessingLayoutController(
    requestModule.requestProcessingUseCase(),
    navigator,
    requestProcessingPresentationMapper
);
```

- [ ] **Step 4: Update `MvcContext`**

Construct request controllers with:

```java
new RequestControllerModule(requestModule, orderModule, navigator)
```

Use `OrderingRoutes.requestProcessingPrefix()` where the dynamic route prefix is checked, or keep the existing private constant if changing that is too broad. The View must not receive route callbacks.

Change request-processing View init to:

```java
requestProcessingView.init(requestControllers.requestProcessingLayoutController());
```

- [ ] **Step 5: Update `RequestProcessingLayoutView`**

Remove:

```java
private Consumer<String> navigateToView = viewId -> {};
```

Change `init` to:

```java
public void init(RequestProcessingLayoutController controller) {
    this.controller = Objects.requireNonNull(controller, "controller");
}
```

Change `goBack()` to:

```java
controller.goBack();
```

Change dialog success navigation to:

```java
controller.openCreatedOrders();
```

- [ ] **Step 6: Static check**

Run:

```powershell
rg -n "navigateToView|Consumer<String>|\"received-requests\"|\"orders\"" src/main/java/org/itss/prj_itss/view/ordering/request/process
```

Expected: no matches in request-processing View classes.

- [ ] **Step 7: Compile and test**

Run:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test -Dtest=MvcDependencyTest
```

Expected:

```text
BUILD SUCCESS
```

---

## Phase E: Update General Class Diagram

**Intent:** The UML must reflect the strict MVC boundary, not the old smart View structure.

**Files:**

- Modify: `Biểu đồ lớp tổng quát.md`

- [ ] **Step 1: Update View layer classes**

For `RequestProcessingLayoutView`:

- Remove `Consumer<String> navigateToView`.
- Do not list internal render helpers as public/general UML responsibilities.
- Keep only UI event entry points and controller reference.

For item/site/preview Views:

- Show dependency on ViewModel records.
- Do not imply they calculate allocation summaries or warning messages.

- [ ] **Step 2: Update Controller layer classes**

For `RequestProcessingLayoutController`, show:

- `Navigator navigator`
- `RequestProcessingSession session`
- `RequestProcessingPresentationMapper presentationMapper`
- `goBack()`
- `openCreatedOrders()`
- `previewDialogController(...)`

Add `RequestProcessingPresentationMapper` with responsibility:

- `toViewModel(...)`
- `toAllocationChangeResultView(...)`
- `toPreviewViewModel(...)`

Add `OrderingRoutes` if it is used by controllers/bootstrapping.

- [ ] **Step 3: Preserve Model responsibilities**

Keep business/domain classes such as:

- `RequestProcessingUseCase`
- `AllocationControl`
- `AllocationValidator`
- `AllocationSuggester`
- `AllocationPlan`

These remain the source of business decisions.

- [ ] **Step 4: Verify diagram scope**

Confirm the diagram does not claim:

- View owns navigation.
- View owns allocation summary logic.
- Controller owns business calculation logic.

---

## Final Verification

- [ ] **Step 1: Compile**

```powershell
.\mvnw.cmd -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: MVC architecture test**

```powershell
.\mvnw.cmd test -Dtest=MvcDependencyTest
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 3: Request-processing View static check**

```powershell
rg -n "navigateToView|Consumer<String>|new .*Controller|request-processing:|received-requests|\.stream\(|switch \(|Math\.abs|mapToInt|flatMap" src/main/java/org/itss/prj_itss/view/ordering/request/process
```

Expected:

- No matches that represent navigation, controller creation, or display calculations in View classes.
- Any remaining match must be reviewed as pure JavaFX/UI iteration and documented in the implementation notes.

- [ ] **Step 4: Confirm `ReceivedRequestsView.java` unchanged**

```powershell
git diff -- src/main/java/org/itss/prj_itss/view/ordering/request/ReceivedRequestsView.java
```

Expected: no diff.

