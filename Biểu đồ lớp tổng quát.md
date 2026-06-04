@startuml
package "View Layer" {
    class ReceivedRequestsView {
        - ObservableList<RequestRow> rows
        - FilteredList<RequestRow> filteredRows
        - ObservableList<RequestRow> paginatedRows
        - int currentPage
        - int pageSize
        - Navigator navigator
        - ReceivedRequestsController controller
        - RequestDetailPopupController detailPopupController
        - OrderDetailController orderDetailController
        - OrderManagementController orderManagementController
        + void init(Navigator, ReceivedRequestsController, RequestDetailPopupController, OrderDetailController, OrderManagementController)
        + void onViewShown()
    }
    class RequestProcessingLayoutView {
        - RequestProcessingLayoutController controller
        - SiteFilterView siteFilterView
        - Consumer<String> navigateToView
        + void init(RequestProcessingLayoutController, Consumer<String>)
        + void setRequestId(int)
        - void goBack()
        - void handleConfirm()
        - void handleOptimizeAllocation()
        - void handleShowAllPlans()
        - void handleSiteFilterChanged()
        - void toggleExpandedItem(int)
    }
    class SiteFilterView {
        - SiteFilterController controller
        - VBox root
        - boolean expanded
        - Runnable onFiltersChanged
        + {static} SiteFilterView load(List<ProcessingSiteView>, Runnable)
        + VBox root()
        + Set<Integer> getSelectedSiteIds()
        + Set<Integer> getExcludedSiteIds()
    }
    class AllSuggestPopupView {
        - List<SuggestedPlanView> plans
        - Consumer<SuggestedPlanView> onApply
        - Stage dialog
        + {static} void show(List<SuggestedPlanView>, Consumer<SuggestedPlanView>)
    }
    class RequestProcessingPreviewDialog {
        - Runnable onOrdersRequested
        - RequestProcessingPreviewDialogController controller
        + void show(Node)
    }
    class RequestProcessingPreviewDialogView {
        - RequestProcessingPreviewDialogController controller
        - Stage dialog
        - Runnable onOrdersRequested
        ~ void init(Stage, Runnable, RequestProcessingPreviewDialogController)
        - void submit()
    }
    class ItemsSectionView {
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
    }
}

package "Controller Layer" {
    class SiteFilterController {
        - SiteFilterModel model
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
        + List<ProcessingPreviewOrderView> previewOrders()
        + SubmitResult submit()
    }
    class RequestProcessingLayoutController {
        - RequestProcessingSession session
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
        + void submitAllocatedOrders()
    }
    class RequestProcessingSession {
        - RequestProcessingUseCase requestProcessingUseCase
        - List<ItemRequirement> items
        - List<SiteStockOption> allSites
        - Map<Integer, Map<Integer, Allocation>> allocations
        - Set<Integer> excludedSiteIds
        - Set<Integer> selectedSiteIds
        - AllocationControl allocationControl
        - List<SuggestedPlan> currentSuggestedPlans
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
        + void submitAllocatedOrders()
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
}

package Model {
    package "Application Layer" {
        class RequestProcessingUseCase {
            - RequestProcessingGateway gateway
            - AllocationValidator allocationValidator
            - AllocationSuggester allocationSuggester
            + RequestProcessingData loadProcessingData(int)
            + List<String> validateAllocations(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + String validateSubmission(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>, int)
            + List<PreviewOrder> buildPreviewOrders(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>)
            + void createAllocatedOrders(int, Map<Integer, Map<Integer, Allocation>>)
            + AllocationSuggester allocationSuggester()
        }
        interface RequestProcessingGateway {
            + RequestProcessingData loadProcessingData(int)
            + void createAllocatedOrders(int, Map<Integer, Map<Integer, Allocation>>)
        }
        interface PreviewBuilder {
            + void reset()
            + PreviewBuilder items(List<ItemRequirement>)
            + PreviewBuilder sites(List<SiteStockOption>)
            + PreviewBuilder allocations(Map<Integer, Map<Integer, Allocation>>)
            + PreviewBuilder desiredDeliveryDates(Map<Integer, LocalDate>)
        }
        class RequestProcessingPreviewBuilder {
            + void reset()
            + RequestProcessingPreviewBuilder items(List<ItemRequirement>)
            + RequestProcessingPreviewBuilder sites(List<SiteStockOption>)
            + RequestProcessingPreviewBuilder allocations(Map<Integer, Map<Integer, Allocation>>)
            + RequestProcessingPreviewBuilder desiredDeliveryDates(Map<Integer, LocalDate>)
            + List<PreviewOrder> getProduct()
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
            + RequestProcessingData loadProcessingData(int)
            + void createAllocatedOrders(int, Map<Integer, Map<Integer, Allocation>>)
        }
    }

    package "Domain Layer" {
        class RequestProcessingData {
            + int requestId()
            + LocalDate earliestDeliveryDate()
            + int deadlineDays()
            + List<ItemRequirement> items()
            + List<SiteStockOption> sites()
            + Map<Integer, LocalDate> desiredDeliveryDates()
        }
        class AllocationControl {
            - List<ItemRequirement> items
            - List<SiteStockOption> allSites
            - Set<Integer> excludedSiteIds
            - Set<Integer> selectedSiteIds
            - Map<Integer, Map<Integer, Allocation>> allocations
            - int deadlineDays
            - ApplyPlan applyPlan
            - AllocationSuggester allocationSuggester
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
        interface AllocationSuggester {
            + Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int)
            + List<SuggestedPlan> buildSuggestedPlans(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int, int, int)
        }
        class DefaultAllocationSuggester {
            - AllocationObjective objective
            + Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int)
            + List<SuggestedPlan> buildSuggestedPlans(List<ItemRequirement>, List<SiteStockOption>, Set<Integer>, Set<Integer>, int, int, int)
        }
        interface AllocationValidator {
            + List<String> validateAllocations(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + String validateSubmission(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>, int)
        }
        class DefaultAllocationValidator {
            + List<String> validateAllocations(List<ItemRequirement>, Map<Integer, Map<Integer, Allocation>>)
            + String validateSubmission(List<ItemRequirement>, List<SiteStockOption>, Map<Integer, Map<Integer, Allocation>>, Map<Integer, LocalDate>, int)
        }
        class Allocation {
            + int siteId
            + int merchandiseId
            - int quantity
            + String transport
            + int getQuantity()
            + void setQuantity(int)
        }
        class AllocationDraft {
            + int siteId()
            + int quantity()
            + String transport()
        }
        class SuggestedPlan {
            + String signature()
            + int totalQuantity()
            + int totalLineCount()
            + int siteCount()
            + int totalDeliveryDays()
            + Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem()
            + List<SiteOrderSuggestion> siteOrders()
        }
        class SiteOrderSuggestion {
            + SiteStockOption site()
            + List<OrderLineSuggestion> lines()
            + int totalQuantity()
            + int deliveryDays()
            + String transportSummary()
        }
        class OrderLineSuggestion {
            + ItemRequirement item()
            + int quantity()
            + String transport()
            + int deliveryDays()
        }
        class ItemVariant {
            + Map<Integer, AllocationDraft> allocationsBySite()
            + int siteCount()
            + int totalDeliveryDays()
            + String signature()
        }
        class AllocationPlan {
            - Map<Integer, Map<Integer, Allocation>> allocations
            + {static} AllocationPlan using(Map<Integer, Map<Integer, Allocation>>)
            + int allocatedQuantity(int)
            + void removeSites(Set<Integer>)
            + Map<Integer, List<Allocation>> groupBySite()
        }
        class ApplyPlan {
            - List<ItemRequirement> items
            - Map<Integer, Map<Integer, Allocation>> allocations
            + void apply(Map<Integer, Map<Integer, AllocationDraft>>)
        }
        class AllocationSuggestEngine {
            - List<ItemRequirement> items
            - List<SiteStockOption> allSites
            - SiteSelectionScope scope
            - AllocationObjective objective
            - int deadlineDays
            + List<SuggestedPlan> suggestMany(int, int)
        }
        interface AllocationObjective {
            + double calculateScore(Map<Integer, Map<Integer, AllocationDraft>>, List<SiteStockOption>)
        }
        class FastDeliveryObjective {
            + double calculateScore(Map<Integer, Map<Integer, AllocationDraft>>, List<SiteStockOption>)
        }
        class SiteSelectionScope {
            - List<SiteStockOption> allSites
            - Set<Integer> excludedSiteIds
            - Set<Integer> selectedSiteIds
            + List<SiteStockOption> candidateSites()
        }
        class SiteFilterModel {
            - List<ProcessingSiteView> allSites
            - List<ProcessingSiteView> visibleSites
            - Set<Integer> selectedSiteIds
            - Set<Integer> excludedSiteIds
            + void setSites(List<ProcessingSiteView>)
            + void select(ProcessingSiteView)
            + void deselect(ProcessingSiteView)
            + void exclude(ProcessingSiteView)
            + void removeSelected(int)
            + void removeExcluded(int)
            + void clearFilters()
            + void refreshVisibleSites(String)
            + List<ProcessingSiteView> allSites()
            + List<ProcessingSiteView> visibleSites()
            + List<ProcessingSiteView> selectedSites()
            + List<ProcessingSiteView> excludedSites()
            + Set<Integer> selectedSiteIds()
            + Set<Integer> excludedSiteIds()
            + boolean isSelected(ProcessingSiteView)
        }
        class AllSuggest {
            - AllocationSuggestEngine engine
            + List<SuggestedPlan> buildSuggestedPlans(int, int)
        }
        class OptimalSuggest {
            - List<SiteStockOption> allSites
            - Set<Integer> excludedSiteIds
            - Set<Integer> selectedSiteIds
            - int deadlineDays
            - AllocationObjective objective
            + Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement>)
        }
    }
}

' --- View Relations ---
ReceivedRequestsView ..> RequestProcessingLayoutView
RequestProcessingLayoutView *-- SiteFilterView
RequestProcessingLayoutView ..> ItemsSectionView
RequestProcessingLayoutView ..> AllSuggestPopupView
RequestProcessingLayoutView ..> RequestProcessingPreviewDialog
RequestProcessingLayoutView --> RequestProcessingLayoutController
ItemsSectionView ..> RequestProcessingLayoutController
SiteFilterView *-- SiteFilterController
SiteFilterView ..> RequestProcessingLayoutView
AllSuggestPopupView ..> RequestProcessingLayoutView
RequestProcessingPreviewDialog *-- RequestProcessingPreviewDialogView
RequestProcessingPreviewDialogView --> RequestProcessingPreviewDialogController

' --- Controller Relations ---
SiteFilterController *-- SiteFilterModel
RequestProcessingLayoutController --> RequestProcessingSession
RequestProcessingSession --> RequestProcessingUseCase
RequestProcessingPreviewDialogController --> RequestProcessingLayoutController

' --- Application & Infrastructure Relations ---
RequestProcessingUseCase --> RequestProcessingGateway
RequestProcessingUseCase --> AllocationValidator
RequestProcessingUseCase --> AllocationSuggester
RequestProcessingUseCase ..> RequestProcessingPreviewBuilder
RequestProcessingPreviewBuilder ..|> PreviewBuilder
JdbcRequestProcessingGateway ..|> RequestProcessingGateway
JdbcRequestProcessingGateway ..> RequestProcessingData

' --- Domain & State Relations ---
RequestProcessingSession *-- AllocationControl
RequestProcessingSession ..> RequestProcessingData
RequestProcessingSession ..> AllocationPlan
DefaultAllocationValidator ..|> AllocationValidator
DefaultAllocationSuggester ..|> AllocationSuggester
DefaultAllocationSuggester ..> AllSuggest
DefaultAllocationSuggester ..> OptimalSuggest
AllocationControl *-- ApplyPlan
AllocationControl --> AllocationSuggester
AllSuggest *-- AllocationSuggestEngine
OptimalSuggest *-- AllocationSuggestEngine
OptimalSuggest ..> SiteSelectionScope
AllSuggest ..> SiteSelectionScope
AllocationSuggestEngine --> AllocationObjective
AllocationSuggestEngine --> SiteSelectionScope
AllocationSuggestEngine ..> ItemVariant
FastDeliveryObjective ..|> AllocationObjective
SuggestedPlan *-- SiteOrderSuggestion
SiteOrderSuggestion *-- OrderLineSuggestion
SiteOrderSuggestion --> SiteStockOption
OrderLineSuggestion --> ItemRequirement
@enduml