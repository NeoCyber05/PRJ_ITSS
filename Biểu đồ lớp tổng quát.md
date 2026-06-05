@startuml
package "View Layer" {
    class ReceivedRequestsView {
        - {static} String EMPTY_MESSAGE
        - {static} String ITEM_LABEL
        - ObservableList<RequestRow> rows
        - FilteredList<RequestRow> filteredRows
        - PaginationSupport<RequestRow> pagination
        - Navigator navigator
        - ReceivedRequestsController controller
        - RequestDetailContext detailContext
        + void init(Navigator, ReceivedRequestsController, RequestDetailContext)
        + void onViewShown()
        - void initialize()
        - void reload()
        - void applyFilters()
        - void goToPrevPage()
        - void goToNextPage()
    }

    class RequestDetailContext <<record>> {
        + RequestDetailPopupController requestController()
        + OrderDetailController orderController()
        + OrderManagementController managementController()
        + Navigator navigator()
    }

    class RequestProcessingLayoutView {
        - RequestProcessingLayoutController controller
        - SiteFilterView siteFilterView
        - Consumer<String> navigateToView
        - Label requestCodeLabel
        - Label requestSummaryLabel
        - Label requestStatusLabel
        - VBox siteFilterContainer
        - VBox itemsTableContainer
        - VBox allocationContainer
        + void init(RequestProcessingLayoutController, Consumer<String>)
        + void setRequestId(int)
        - void goBack()
        - void handleConfirm()
        - void showPreviewDialog(List<ProcessingPreviewOrderView>)
        - void renderProcessingScreen()
        - void renderHeader()
        - void renderSiteFilterSection()
        - void hideLegacyAllocationContainer()
        - void renderItemsViewSection()
        - void handleOptimizeAllocation()
        - void handleShowAllPlans()
        - void applySelectedPlan(SuggestedPlanView)
        - void handleSiteFilterChanged()
        - void toggleExpandedItem(int)
        - void showValidationError(String)
        - void styleDialog(Alert)
    }

    class SiteFilterView {
        - {static} String VIEW_RESOURCE
        - {static} List<String> TOGGLE_STATE_CLASSES
        - SiteFilterController controller
        - VBox siteListContainer
        - VBox filterContent
        - HBox toggleGraphic
        - HBox priorityTagsBox
        - HBox excludeTagsBox
        - TextField searchBox
        - Label countLabel
        - Label toggleSummaryLabel
        - Label toggleChevronLabel
        - Button toggleButton
        - Button clearAllButton
        - VBox root
        - boolean expanded
        - Runnable onFiltersChanged
        + {static} SiteFilterView load(List<ProcessingSiteView>, Runnable)
        + VBox root()
        + Set<Integer> getSelectedSiteIds()
        + Set<Integer> getExcludedSiteIds()
        - void initialize()
        - void init(List<ProcessingSiteView>, Runnable)
        - void clearAllFilters()
        - void onSelectionToggled(ProcessingSiteView)
        - void onExclude(ProcessingSiteView)
        - void renderUi()
        - void renderSiteList()
        - void renderSelectedTags()
        - void renderExcludeTags()
        - void renderSummary()
        - void renderExpandedState()
        - void notifyFiltersChanged()
        - {static} String siteName(ProcessingSiteView)
    }

    class AllSuggestPopupView {
        - {static} String POPUP_RESOURCE
        - {static} String EMPTY_CARD_RESOURCE
        - Label titleLabel
        - Label subtitleLabel
        - VBox plansBox
        - Button closeButton
        - List<SuggestedPlanView> plans
        - Consumer<SuggestedPlanView> onApply
        - Stage dialog
        + {static} void show(List<SuggestedPlanView>, Consumer<SuggestedPlanView>)
        - void init(Stage, List<SuggestedPlanView>, Consumer<SuggestedPlanView>)
        - String buildSubtitle()
        - void renderPlans()
        - void applyPlan(SuggestedPlanView, int)
        - VBox loadEmptyCard()
    }

    class RequestProcessingPreviewDialog {
        - {static} String VIEW_RESOURCE
        - Runnable onOrdersRequested
        - RequestProcessingPreviewDialogController controller
        + RequestProcessingPreviewDialog(Runnable, RequestProcessingPreviewDialogController)
        + void show(Node)
        - Parent loadRoot(Stage)
        - Window resolveOwnerWindow(Node)
    }

    class RequestProcessingPreviewDialogView {
        - Label subtitleLabel
        - VBox ordersBox
        - Button backButton
        - Button sendButton
        - Stage dialog
        - Runnable onOrdersRequested
        - RequestProcessingPreviewDialogController controller
        ~ void init(Stage, Runnable, RequestProcessingPreviewDialogController)
        - void render(List<ProcessingPreviewOrderView>)
        - void submit()
        - void showCreationError()
    }

    class ItemsSectionView {
        - {static} String VIEW_RESOURCE
        - Button optimizeButton
        - Button showAllButton
        - VBox root
        - RequestProcessingViewModel viewModel
        - Runnable onOptimizeRequested
        - Runnable onShowAllPlansRequested
        - IntConsumer onToggleExpandedItem
        - Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged
        - List<ItemsSectionItemRowView> itemRowViews
        + {static} ItemsSectionView load(RequestProcessingViewModel, Runnable, Runnable, IntConsumer, Function<AllocationChangeCommand, AllocationChangeResultView>)
        + VBox root()
        + void refreshAllocationLabels()
        - void handleOptimize()
        - void handleShowAllPlans()
        - void init(RequestProcessingViewModel, Runnable, Runnable, IntConsumer, Function<AllocationChangeCommand, AllocationChangeResultView>)
        - void renderItems()
        - void handleItemAllocationChanged(int)
    }

    class SiteFilterState {
        - List<ProcessingSiteView> allSites
        - List<ProcessingSiteView> visibleSites
        - Set<Integer> selectedSiteIds
        - Set<Integer> excludedSiteIds
        + void setSites(List<ProcessingSiteView>)
        + List<ProcessingSiteView> allSites()
        + List<ProcessingSiteView> visibleSites()
        + Set<Integer> selectedSiteIds()
        + Set<Integer> excludedSiteIds()
        + void clearFilters()
        + void select(ProcessingSiteView)
        + void deselect(ProcessingSiteView)
        + void exclude(ProcessingSiteView)
        + void removeSelected(int)
        + void removeExcluded(int)
        + boolean isSelected(ProcessingSiteView)
        + void refreshVisibleSites(String)
        + List<ProcessingSiteView> selectedSites()
        + List<ProcessingSiteView> excludedSites()
        - List<ProcessingSiteView> sitesFor(Set<Integer>)
        - {static} Optional<ProcessingSiteView> findSite(List<ProcessingSiteView>, int)
        - {static} boolean matches(ProcessingSiteView, String)
        - {static} String normalizeKeyword(String)
        - {static} String normalizeText(String)
    }

    class RequestProcessingViewModel <<record>> {
        + int requestId
        + String requestCode
        + String earliestDeliveryDate
        + int deadlineDays
        + List<ProcessingItemView> items
        + List<ProcessingSiteView> sites
        + Map<Integer, String> desiredDeliveryDates
        + List<AllocationItemViewModel> allocationItems
    }

    class AllocationItemViewModel <<record>> {
        + int merchandiseId
        + String code
        + String name
        + int required
        + int allocated
        + int totalStock
        + String allocationStatusText
        + String allocationFractionText
        + boolean expanded
        + List<AllocationSiteRowViewModel> siteRows
    }

    class AllocationSiteRowViewModel <<record>> {
        + int itemMerchandiseId
        + int siteId
        + String siteName
        + String siteDetail
        + int stock
        + int quantity
        + String selectedTransportLabel
        + List<String> transportLabels
        + boolean transportDisabled
        + String deliveryStatusText
        + String deliveryStatusClass
    }

    class ProcessingSiteView <<record>> {
        + int id
        + String siteCode
        + String name
        + String description
        + Integer shipDays
        + Integer airDays
        + Map<Integer, Integer> stock
    }

    class ProcessingItemView <<record>> {
        + int merchandiseId
        + String code
        + String name
        + int required
    }

    class ProcessingPreviewOrderView <<record>> {
        + String siteName
        + String siteCode
        + List<ProcessingPreviewLineView> lines
    }

    class ProcessingPreviewLineView <<record>> {
        + String merchandiseCode
        + String merchandiseName
        + int quantity
        + String transport
        + String desiredDate
        + String estimatedDate
    }

    class SuggestedPlanView <<record>> {
        + String signature
        + int totalQuantity
        + int totalLineCount
        + int siteCount
        + int totalDeliveryDays
    }

    class AllocationChangeCommand <<record>> {
        + int itemMerchandiseId
        + int siteId
        + String quantityText
        + String transportLabel
    }

    class AllocationChangeResultView <<record>> {
        + boolean applied
        + String errorType
        + int stock
        + int deliveryDays
        + int dayDelta
        + boolean deliveryAvailable
        + String deliveryStatusText
        + String deliveryStatusClass
    }

    class ItemsSectionItemRowView {
        - {static} String VIEW_RESOURCE
        - Label codeLabel
        - Label nameLabel
        - Label requiredLabel
        - Label deadlineLabel
        - Label allocationStatusLabel
        - Label allocationFractionLabel
        - Label stockValueLabel
        - Button toggleButton
        - HBox root
        - int itemIndex
        - IntConsumer onToggle
        + {static} ItemsSectionItemRowView load(AllocationItemViewModel, int, String, IntConsumer)
        + HBox root()
        + void refresh(AllocationItemViewModel)
        - void handleToggle()
        - void init(AllocationItemViewModel, int, String, IntConsumer)
        - void updateAllocationLabels(AllocationItemViewModel)
    }

    class AllocationItemEditorView {
        - {static} String VIEW_RESOURCE
        - Label titleLabel
        - Label subtitleLabel
        - Label allocatedBadgeLabel
        - Label remainingBadgeLabel
        - VBox siteRowsBox
        - Label emptyLabel
        - AllocationItemViewModel item
        - int itemIndex
        - List<AllocationSiteRowViewModel> siteRows
        - Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged
        - IntConsumer onItemAllocationChanged
        + {static} VBox load(AllocationItemViewModel, int, List<AllocationSiteRowViewModel>, Function<AllocationChangeCommand, AllocationChangeResultView>, IntConsumer)
        - void init(AllocationItemViewModel, int, List<AllocationSiteRowViewModel>, Function<AllocationChangeCommand, AllocationChangeResultView>, IntConsumer)
        - void renderSiteRows()
        - void refreshSummaryBadges()
        - int countAvailableSites()
    }

    class AllocationSiteRowView {
        - {static} String VIEW_RESOURCE
        - Label siteNameLabel
        - Label siteStockLabel
        - TextField quantityField
        - ComboBox<String> transportCombo
        - Label deliveryDaysLabel
        - HBox root
        - AllocationSiteRowViewModel siteRow
        - Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged
        - Runnable onRowChanged
        + {static} HBox load(AllocationSiteRowViewModel, Function<AllocationChangeCommand, AllocationChangeResultView>, Runnable)
        - void initialize()
        - void init(AllocationSiteRowViewModel, Function<AllocationChangeCommand, AllocationChangeResultView>, Runnable)
        - void handleQuantityChanged(String)
        - void handleTransportChanged(String)
        - void handleResult(AllocationChangeResultView)
    }

    class PreviewOrderCardView {
        - {static} String VIEW_RESOURCE
        - Label siteNameLabel
        - VBox tableRowsBox
        - HBox root
        + {static} HBox load(ProcessingPreviewOrderView)
        + HBox root()
        - void init(ProcessingPreviewOrderView)
    }

    class PreviewTableRowView {
        - {static} String VIEW_RESOURCE
        - Label codeLabel
        - Label nameLabel
        - Label quantityLabel
        - Label transportLabel
        - Label desiredDateLabel
        - Label estimatedDateLabel
        - HBox root
        + {static} HBox load(ProcessingPreviewLineView)
        + HBox root()
        - void init(ProcessingPreviewLineView)
    }
}

package "Controller Layer" {
    class SiteFilterController {
        - SiteFilterState model
        - String keyword
        + void init(List<ProcessingSiteView>)
        + void search(String)
        + void clearAllFilters()
        + void selectSite(ProcessingSiteView)
        + void deselectSite(ProcessingSiteView)
        + void excludeSite(ProcessingSiteView)
        + void removeSelected(int)
        + void removeExcluded(int)
        + List<ProcessingSiteView> allSites()
        + List<ProcessingSiteView> visibleSites()
        + List<ProcessingSiteView> selectedSites()
        + List<ProcessingSiteView> excludedSites()
        + Set<Integer> selectedSiteIds()
        + Set<Integer> excludedSiteIds()
        + boolean isSelected(ProcessingSiteView)
        + String keyword()
        - void refreshVisibleSites()
    }

    class RequestProcessingPreviewDialogController {
        - RequestProcessingLayoutController requestProcessingLayoutController
        - List<ProcessingPreviewOrderView> previewOrders
        + RequestProcessingPreviewDialogController(RequestProcessingLayoutController, List<ProcessingPreviewOrderView>)
        + List<ProcessingPreviewOrderView> previewOrders()
        + SubmitResult submit()
    }

    class SubmitResult <<record>> {
        + boolean success()
    }

    class RequestProcessingLayoutController {
        - RequestProcessingSession session
        + RequestProcessingLayoutController(RequestProcessingUseCase)
        + void setRequestId(int)
        + RequestProcessingViewModel snapshot()
        + void handleSiteFilterChanged(Set<Integer>, Set<Integer>)
        + void handleOptimizeAllocation()
        + List<SuggestedPlanView> handleShowAllPlans()
        + void applySelectedPlan(String)
        + AllocationChangeResultView handleAllocationInputChanged(AllocationChangeCommand)
        + void toggleExpandedItem(int)
        + ConfirmResult handleConfirm()
        + String validateCurrentSubmission()
        + List<ProcessingPreviewOrderView> buildPreviewOrders()
        + void submitAllocatedOrders() throws RequestProcessingException
    }

    class RequestProcessingSession {
        - RequestProcessingUseCase requestProcessingUseCase
        - List<ItemRequirement> items
        - List<SiteStockOption> allSites
        - Map<Integer, Map<Integer, Allocation>> allocations
        - Map<Integer, LocalDate> desiredDeliveryDates
        - Set<Integer> excludedSiteIds
        - Set<Integer> selectedSiteIds
        - int requestId
        - int deadlineDays
        - int expandedItemIndex
        - LocalDate earliestDeliveryDate
        - AllocationControl allocationControl
        - List<SuggestedPlan> currentSuggestedPlans
        + RequestProcessingSession(RequestProcessingUseCase)
        + void start(int)
        + int requestId()
        + String requestCode()
        + RequestProcessingViewModel buildViewModel()
        + void handleSiteFilterChanged(Set<Integer>, Set<Integer>)
        + void handleOptimizeAllocation()
        + List<SuggestedPlanView> handleShowAllPlans()
        + void applySelectedPlanBySignature(String)
        + AllocationChangeResultView handleAllocationInputChanged(AllocationChangeCommand)
        + void toggleExpandedItem(int)
        + ConfirmResult handleConfirm()
        + String validateCurrentSubmission()
        + List<ProcessingPreviewOrderView> buildPreviewOrderViews()
        + void submitAllocatedOrders() throws RequestProcessingException
        + boolean isSiteExcluded(int)
        + boolean isSiteSelected(int)
        + Set<Integer> excludedSiteIds()
        + Set<Integer> selectedSiteIds()
        + int expandedItemIndex()
        - void resetProcessingState()
        - void loadProcessingData()
        - void rebuildAllocationSection()
        - AllocationControl createAllocationControl()
        - int getAllocated(int)
        - Set<Integer> copyIds(Set<Integer>)
    }

    class "RequestProcessingLayoutController.ConfirmResult" as LayoutConfirmResult <<record>> {
        + String validationMessage()
        + List<ProcessingPreviewOrderView> previewOrders()
        + {static} LayoutConfirmResult invalid(String)
        + {static} LayoutConfirmResult valid(List<ProcessingPreviewOrderView>)
        + boolean valid()
    }

    class "RequestProcessingSession.ConfirmResult" as SessionConfirmResult <<record>> {
        + String validationMessage()
        + List<ProcessingPreviewOrderView> previewOrders()
        + {static} SessionConfirmResult invalid(String)
        + {static} SessionConfirmResult valid(List<ProcessingPreviewOrderView>)
        + boolean valid()
    }
}

package Model {
    package "Application Layer" {
        class RequestProcessingUseCase {
            - RequestProcessingGateway gateway
            - AllocationValidator allocationValidator
            - AllocationSuggester allocationSuggester
            + RequestProcessingUseCase(RequestProcessingGateway, AllocationValidator, AllocationSuggester)
            + RequestProcessingData loadProcessingData(int)
            + List<String> validateAllocations(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + String validateSubmission(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>)
            + List<RequestProcessingPreviewBuilder.PreviewOrder> buildPreviewOrders(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>)
            + void createAllocatedOrders(int, Map<Integer, Map<Integer, Allocation>>) throws RequestProcessingException
            + AllocationSuggester allocationSuggester()
        }

        interface RequestProcessingGateway {
            + RequestProcessingData loadProcessingData(int)
            + void createAllocatedOrders(int, Map<Integer, Map<Integer, Allocation>>) throws RequestProcessingGatewayException
        }

        interface PreviewBuilder {
            + void reset()
            + PreviewBuilder items(List<ItemRequirement>)
            + PreviewBuilder sites(List<SiteStockOption>)
            + PreviewBuilder allocations(Map<Integer, Map<Integer, Allocation>>)
            + PreviewBuilder desiredDeliveryDates(Map<Integer, LocalDate>)
        }

        class RequestProcessingPreviewBuilder {
            - List<ItemRequirement> items
            - List<SiteStockOption> sites
            - Map<Integer, Map<Integer, Allocation>> allocations
            - Map<Integer, LocalDate> desiredDeliveryDates
            + RequestProcessingPreviewBuilder()
            + void reset()
            + RequestProcessingPreviewBuilder items(List<ItemRequirement>)
            + RequestProcessingPreviewBuilder sites(List<SiteStockOption>)
            + RequestProcessingPreviewBuilder allocations(Map<Integer, Map<Integer, Allocation>>)
            + RequestProcessingPreviewBuilder desiredDeliveryDates(Map<Integer, LocalDate>)
            + List<PreviewOrder> getProduct()
        }

        class PreviewOrder <<record>> {
            + SiteStockOption site()
            + List<PreviewLine> lines()
        }

        class PreviewLine <<record>> {
            + ItemRequirement item()
            + int quantity()
            + String transport()
            + LocalDate desiredDate()
            + LocalDate estimatedDate()
        }
    }

    package "Infrastructure Layer" {
        class JdbcRequestProcessingGateway {
            - ProcessingRequestPort requestRepository
            - OrderRepository orderRepository
            - SiteRepository siteRepository
            - InventoryRepository inventoryRepository
            - MerchandiseRepository merchandiseRepository
            - TransactionRunner transactionRunner
            + JdbcRequestProcessingGateway(ProcessingRequestPort, OrderRepository, SiteRepository, InventoryRepository, MerchandiseRepository, TransactionRunner)
            + RequestProcessingData loadProcessingData(int)
            + void createAllocatedOrders(int, Map<Integer, Map<Integer, Allocation>>) throws RequestProcessingGatewayException
            - String toStoredDeliveryMethod(String)
        }
    }

    package "Domain Layer" {
        class RequestProcessingData <<record>> {
            + int requestId()
            + LocalDate earliestDeliveryDate()
            + int deadlineDays()
            + List<ItemRequirement> items()
            + List<SiteStockOption> sites()
            + Map<Integer, LocalDate> desiredDeliveryDates()
        }

        class AllocationControl {
            - {static} int MAX_SUGGESTED_PLANS
            - {static} int MAX_ITEM_VARIANTS
            - List<ItemRequirement> items
            - List<SiteStockOption> allSites
            - Set<Integer> excludedSiteIds
            - Set<Integer> selectedSiteIds
            - Map<Integer, Map<Integer, Allocation>> allocations
            - int deadlineDays
            - ApplyPlan applyPlan
            - AllocationSuggester allocationSuggester
            + AllocationControl(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, Map<Integer, Map<Integer, Allocation>>, int, AllocationSuggester)
            + int getAllocated(int)
            + ItemAllocationSummary allocationSummary(ItemRequirement)
            + AllocationSiteRowState siteRowState(ItemRequirement, SiteStockOption)
            + AllocationChangeResult applyAllocationChange(AllocationChangeRequest)
            + void applyOptimalAllocation()
            + List<SuggestedPlan> buildSuggestedPlans()
            + void applySelectedPlan(SuggestedPlan)
            - List<String> transportLabels(SiteStockOption)
            - DeliveryStatus deliveryStatus(SiteStockOption, String)
            - Integer parseQuantity(String)
            - void updateAllocationsState(ItemRequirement, SiteStockOption, int, String)
            - {static} String siteName(SiteStockOption)
            - {static} String siteDetail(SiteStockOption)
        }

        enum ItemAllocationState {
            NONE
            PARTIAL
            COMPLETE
            OVER
        }

        enum AllocationInputError {
            NONE
            INVALID_INTEGER
            NEGATIVE_QUANTITY
            EXCEEDS_STOCK
        }

        class ItemAllocationSummary <<record>> {
            + int allocated()
            + int required()
            + ItemAllocationState state()
        }

        class AllocationSiteRowState <<record>> {
            + int siteId()
            + String siteName()
            + String siteDetail()
            + int stock()
            + int quantity()
            + String selectedTransportLabel()
            + List<String> transportLabels()
            + DeliveryStatus deliveryStatus()
            + boolean transportDisabled()
        }

        class AllocationChangeRequest <<record>> {
            + ItemRequirement item()
            + SiteStockOption site()
            + String quantityText()
            + String transportLabel()
        }

        class AllocationChangeResult <<record>> {
            + boolean applied()
            + AllocationInputError error()
            + int stock()
            + DeliveryStatus deliveryStatus()
            + {static} AllocationChangeResult applied(int, DeliveryStatus)
            + {static} AllocationChangeResult rejected(AllocationInputError, int, DeliveryStatus)
        }

        class DeliveryStatus <<record>> {
            + int deliveryDays()
            + int dayDelta()
            + boolean available()
        }

        interface AllocationSuggester {
            + Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int)
            + List<SuggestedPlan> buildSuggestedPlans(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int, int, int)
        }

        class DefaultAllocationSuggester {
            - AllocationObjective objective
            + DefaultAllocationSuggester(AllocationObjective)
            + Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int)
            + List<SuggestedPlan> buildSuggestedPlans(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int, int, int)
        }

        interface AllocationValidator {
            + List<String> validateAllocations(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + String validateSubmission(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>)
        }

        class DefaultAllocationValidator {
            + List<String> validateAllocations(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + String validateSubmission(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>)
        }

        class Allocation {
            + int siteId
            + int merchandiseId
            - int quantity
            + String transport
            + Allocation(int, int, int, String)
            + int getQuantity()
            + void setQuantity(int)
        }

        class AllocationDraft <<record>> {
            + int siteId()
            + int merchandiseId()
            + int quantity()
            + String transport()
        }

        class SuggestedPlan <<record>> {
            + Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem()
            + List<SiteOrderSuggestion> siteOrders()
            + int totalQuantity()
            + int totalLineCount()
            + int siteCount()
            + int totalDeliveryDays()
            + String signature()
        }

        class SiteOrderSuggestion <<record>> {
            + SiteStockOption site()
            + List<OrderLineSuggestion> lines()
            + int totalQuantity()
            + int deliveryDays()
            + String transportSummary()
        }

        class OrderLineSuggestion <<record>> {
            + ItemRequirement item()
            + int quantity()
            + String transport()
            + int deliveryDays()
        }

        class ItemVariant <<record>> {
            + Map<Integer, AllocationDraft> allocationsBySite()
            + int siteCount()
            + int totalDeliveryDays()
            + String signature()
        }

        class AllocationPlan {
            - Map<Integer, Map<Integer, Allocation>> allocations
            - AllocationPlan(Map<Integer, Map<Integer, Allocation>>)
            + {static} AllocationPlan using(Map<Integer, Map<Integer, Allocation>>)
            + int allocatedQuantity(int)
            + void removeSites(Set<Integer>)
            + Map<Integer, List<Allocation>> groupBySite()
        }

        class ApplyPlan {
            - List<ItemRequirement> items
            - Map<Integer, Map<Integer, Allocation>> allocations
            + ApplyPlan(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + void apply(Map<Integer, Map<Integer, AllocationDraft>>)
            - void clearCurrentAllocations()
        }

        class AllocationSuggestEngine {
            - {static} int MAX_COMBINATION_ATTEMPTS
            - List<ItemRequirement> items
            - List<SiteStockOption> allSites
            - SiteSelectionScope scope
            - AllocationObjective objective
            - int deadlineDays
            + AllocationSuggestEngine(List<ItemRequirement>, List<SiteStockOption>, SiteSelectionScope, AllocationObjective, int)
            + List<SuggestedPlan> suggestMany(int, int)
            - List<List<ItemVariant>> collectVariantsForAllItems(int)
            - List<ItemVariant> buildItemVariants(ItemRequirement, int)
            - List<List<SiteStockOption>> buildSiteOrderings(ItemRequirement, List<SiteStockOption>)
            - int bestDeliveryDays(SiteStockOption)
            - void addOrdering(List<SiteStockOption>, List<List<SiteStockOption>>, Set<String>)
            - List<SiteStockOption> rotate(List<SiteStockOption>, int)
            - void collectItemVariants(ItemRequirement, List<SiteStockOption>, int, int, Map<Integer, AllocationDraft>, int, List<ItemVariant>, int)
            - int calculateRemainingCapacity(ItemRequirement, List<SiteStockOption>, int)
            - List<Integer> buildQuantityChoices(int, int, int, int)
            - int clamp(int, int, int)
            - ItemVariant buildItemVariant(Map<Integer, AllocationDraft>)
            - long computeMaxAttempts(List<List<ItemVariant>>, int)
            - SuggestedPlan buildPlanFromVariantIndex(List<List<ItemVariant>>, long)
            - SuggestedPlan assemblePlan(Map<Integer, Map<Integer, AllocationDraft>>)
            - SiteOrderSuggestion toSiteOrderSuggestion(MutableSiteOrder)
            - String transportLabel(String)
            - SiteStockOption findSiteById(int)
            - int getDeliveryDays(SiteStockOption, String)
        }

        interface AllocationObjective {
            + String pickTransport(SiteStockOption, int)
            + Comparator<SiteStockOption> siteComparator(ItemRequirement, int)
            + Comparator<ItemVariant> itemVariantComparator()
            + Comparator<SuggestedPlan> planComparator()
        }

        class FastDeliveryObjective {
            + String pickTransport(SiteStockOption, int)
            + Comparator<SiteStockOption> siteComparator(ItemRequirement, int)
            + Comparator<ItemVariant> itemVariantComparator()
            + Comparator<SuggestedPlan> planComparator()
            - int transportRank(SiteStockOption, int)
        }

        class SiteSelectionScope {
            - List<SiteStockOption> allSites
            - Set<Integer> excludedSiteIds
            - Set<Integer> selectedSiteIds
            + SiteSelectionScope(List<SiteStockOption>, Set<Integer>, Set<Integer>)
            + List<SiteStockOption> candidateSites()
        }

        class AllSuggest {
            - AllocationSuggestEngine engine
            + AllSuggest(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int, AllocationObjective)
            + List<SuggestedPlan> buildSuggestedPlans(int, int)
        }

        class OptimalSuggest {
            - List<SiteStockOption> allSites
            - Set<Integer> excludedSiteIds
            - Set<Integer> selectedSiteIds
            - int deadlineDays
            - AllocationObjective objective
            + OptimalSuggest(List<SiteStockOption>, Set<Integer>, Set<Integer>, int, AllocationObjective)
            + Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement>)
        }
    }
}

' --- View Relations ---
ReceivedRequestsView --> Navigator
ReceivedRequestsView --> ReceivedRequestsController
ReceivedRequestsView --> RequestDetailContext
RequestDetailContext --> RequestDetailPopupController
RequestDetailContext --> OrderDetailController
RequestDetailContext --> OrderManagementController
RequestDetailContext --> Navigator
RequestProcessingLayoutView *-- SiteFilterView
RequestProcessingLayoutView ..> ItemsSectionView
RequestProcessingLayoutView ..> AllSuggestPopupView
RequestProcessingLayoutView ..> RequestProcessingPreviewDialog
RequestProcessingLayoutView ..> RequestProcessingPreviewDialogController
RequestProcessingLayoutView --> RequestProcessingLayoutController
SiteFilterView *-- SiteFilterController
AllSuggestPopupView ..> SuggestedPlanView
RequestProcessingPreviewDialog *-- RequestProcessingPreviewDialogView
RequestProcessingPreviewDialog --> RequestProcessingPreviewDialogController
RequestProcessingPreviewDialogView --> RequestProcessingPreviewDialogController
ItemsSectionView ..> RequestProcessingViewModel
ItemsSectionView ..> AllocationChangeCommand
ItemsSectionView ..> AllocationChangeResultView

' --- Controller Relations ---
SiteFilterController *-- SiteFilterState
RequestProcessingLayoutController *-- RequestProcessingSession
RequestProcessingSession --> RequestProcessingUseCase
RequestProcessingPreviewDialogController --> RequestProcessingLayoutController
RequestProcessingLayoutController ..> LayoutConfirmResult
RequestProcessingSession ..> SessionConfirmResult
RequestProcessingPreviewDialogController ..> SubmitResult

' --- Application & Infrastructure Relations ---
RequestProcessingUseCase --> RequestProcessingGateway
RequestProcessingUseCase --> AllocationValidator
RequestProcessingUseCase --> AllocationSuggester
RequestProcessingUseCase ..> RequestProcessingPreviewBuilder
RequestProcessingPreviewBuilder ..|> PreviewBuilder
RequestProcessingPreviewBuilder *-- PreviewOrder
PreviewOrder *-- PreviewLine
JdbcRequestProcessingGateway ..|> RequestProcessingGateway
JdbcRequestProcessingGateway ..> RequestProcessingData

' --- Domain & State Relations ---
RequestProcessingSession *-- AllocationControl
RequestProcessingSession ..> RequestProcessingData
RequestProcessingSession ..> AllocationPlan
DefaultAllocationValidator ..|> AllocationValidator
DefaultAllocationSuggester ..|> AllocationSuggester
DefaultAllocationSuggester --> AllocationObjective
DefaultAllocationSuggester ..> AllSuggest
DefaultAllocationSuggester ..> OptimalSuggest
AllocationControl *-- ApplyPlan
AllocationControl --> AllocationSuggester
AllocationControl ..> ItemAllocationSummary
AllocationControl ..> AllocationSiteRowState
AllocationControl ..> AllocationChangeRequest
AllocationControl ..> AllocationChangeResult
AllocationControl ..> DeliveryStatus
AllocationControl ..> ItemAllocationState
AllocationControl ..> AllocationInputError
AllSuggest *-- AllocationSuggestEngine
OptimalSuggest ..> AllocationSuggestEngine
OptimalSuggest ..> SiteSelectionScope
AllSuggest ..> SiteSelectionScope
AllocationSuggestEngine --> AllocationObjective
AllocationSuggestEngine --> SiteSelectionScope
AllocationSuggestEngine ..> ItemVariant
AllocationSuggestEngine ..> SiteOrderSuggestion
AllocationSuggestEngine ..> OrderLineSuggestion
FastDeliveryObjective ..|> AllocationObjective
SuggestedPlan *-- SiteOrderSuggestion
SiteOrderSuggestion *-- OrderLineSuggestion
SiteOrderSuggestion --> SiteStockOption
OrderLineSuggestion --> ItemRequirement
AllocationPlan --> Allocation
ApplyPlan --> Allocation
ApplyPlan --> AllocationDraft

' --- View & State Internal Relations ---
RequestProcessingViewModel *-- ProcessingItemView
RequestProcessingViewModel *-- ProcessingSiteView
RequestProcessingViewModel *-- AllocationItemViewModel
AllocationItemViewModel *-- AllocationSiteRowViewModel
ProcessingPreviewOrderView *-- ProcessingPreviewLineView
ItemsSectionView *-- ItemsSectionItemRowView
ItemsSectionView *-- AllocationItemEditorView
AllocationItemEditorView *-- AllocationSiteRowView
RequestProcessingPreviewDialogView *-- PreviewOrderCardView
PreviewOrderCardView *-- PreviewTableRowView
@enduml
