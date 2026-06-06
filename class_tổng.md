@startuml
title Biểu đồ lớp tổng hợp các UC yêu cầu đặt hàng và đơn hàng giao tới

legend right
Phạm vi use case:
- Tạo yêu cầu đặt hàng
- Sửa yêu cầu đặt hàng
- Xử lý yêu cầu đặt hàng
- Xử lý đơn hàng bị hủy
- Xem chi tiết yêu cầu đặt hàng đã nhận
- Xác nhận đơn hàng giao tới
endlegend

package "View Layer" {
    package "Sales - Request Management" {
        class SalesRequestListView
        class SalesRequestCreationDialog
        class SalesRequestCreationView
        class SalesRequestCreationItemRow
        class SalesRequestEditDialog
        class SalesRequestEditView
        class SalesRequestEditTableComponent
        class SalesRequestEditItemRow
        interface SalesRequestEditDialogLauncher
        interface SalesRequestEditScreenStarter
        interface SalesRequestEditActionHandler
    }

    package "Ordering - Received Request Detail" {
        class ReceivedRequestsView
        class RequestDetailPopup
        class RequestDetailPopupView
        class RequestItemTableView
        class AllocatedOrderTableView
        class OrderDetailView
    }

    package "Ordering - Request Processing" {
        class RequestProcessingLayoutView
        class SiteFilterView
        class ItemsSectionView
        class AllSuggestPopupView
        class RequestProcessingPreviewDialog
        class RequestProcessingPreviewDialogView
    }

    package "Ordering - Cancelled Order Processing" {
        class OrderCancellationLayoutView
        class OrderAllocationView
        class OrderSuggestionPopupView
        class OrderPreviewView
        class ConfirmSubmitPopupView
        class DetailNotificationPopupView
    }

    package "Warehouse - Arrival Confirmation" {
        class ConfirmOrderArrivalView
    }

    interface Navigator
}

package "Controller Layer" {
    package "Sales Controllers" {
        class SalesRequestListController
        class SalesRequestCreationController {
            + createRequest(items) ActionResult
            + getMerchandiseOptionByCode(code) MerchandiseOptionDTO
            + getAvailableStock(code) int
        }
        class SalesRequestEditController {
            + start(view, input, listener)
            + addItemRequested()
            + saveRequested()
            + cancelRequested()
        }
        class SalesRequestEditSession {
            + start(input, listener) StartResult
            + buildViewModel() SalesRequestEditViewState
            + handleSave() SaveResult
            + handleCancel()
        }
        class SalesRequestEditServiceGateway
        class SalesRequestEditViewState <<record>>
        class SalesRequestEditDialogInput <<record>>
    }

    package "Ordering Controllers" {
        class ReceivedRequestsController {
            + findRows() List
        }
        class RequestDetailContext <<record>>
        class RequestDetailPopupController {
            + load(requestCode) ReceivedRequestDetailViewModel
            + cancel(orderId) ActionResult
            + findOrderRow(orderId) AllocatedOrderRow
        }
        class RequestProcessingLayoutController {
            + setRequestId(requestId)
            + snapshot() RequestProcessingState
            + handleOptimizeAllocation()
            + handleShowAllPlans() List
            + handleConfirm() ConfirmResult
            + submitAllocatedOrders()
        }
        class RequestProcessingSession {
            + start(requestId)
            + buildState() RequestProcessingState
            + handleSiteFilterChanged(excludedIds, selectedIds)
            + buildPreviewOrders() List
            + submitAllocatedOrders()
        }
        class OrderCancellationProcessingController {
            + start(cancelledOrderId)
            + buildViewModel() CancelledOrderProcessingViewModel
            + handleConfirm() ConfirmResult
            + handleSubmit()
        }
        class CancelledOrderProcessingSession {
            + start(cancelledOrderId)
            + buildViewModel() CancelledOrderProcessingViewModel
            + buildPreviewOrders() List
            + submitAllocatedOrders()
        }
        class RequestProcessingState <<record>>
        class ProcessingPreviewOrder <<record>>
        class SuggestedPlanState <<record>>
        class CancelledOrderProcessingViewModel <<record>>
    }

    package "Warehouse Controllers" {
        class ConfirmOrderArrivalController {
            + findInboundOrders() List
            + findItemsByOrderId(orderId) List
            + validateInspection(items, note) ValidationResult
            + confirmArrival(orderId, items, note) ConfirmationResult
        }
        class "ConfirmOrderArrivalController.InspectionItemDto" as InspectionItemDto <<record>>
    }

    class ActionResult <<record>>
    class ValidationResult <<record>>
}

package Model {
    package "Application Layer - Sales Request" {
        interface CreateSalesRequestUseCase {
            + createRequest(items, note) int
        }
        class CreateSalesRequestService {
            + createRequest(items, note) int
        }
        class SalesRequestQueryService {
            + findMerchandiseOptions() List
            + findFormView(requestId) RequestFormView
            + getAvailableStock(code) int
        }
        class SalesRequestCommandService {
            + createRequest(items, note) int
            + updateRequest(requestId, items, note)
        }
        interface SalesRequestQueryPort
        interface SalesRequestCommandPort
        class SalesRequestItemSubmission <<record>>
        class MerchandiseOption <<record>>
        class RequestFormView <<record>>
    }

    package "Application Layer - Sales Request Update" {
        class SalesRequestEditUseCase {
            + loadEditData(requestId) SalesRequestEditData
            + buildEditState(form) SalesRequestEditState
            + validateDraft(draft, today) SalesRequestEditValidationResult
            + updateRequest(draft, today)
        }
        interface SalesRequestEditGateway
        class SalesRequestEditMapper
        class SalesRequestEditValidator
        class SalesRequestEditState
        class SalesRequestEditData <<record>>
        class SalesRequestEditDraft <<record>>
        class SalesRequestEditItemDraft <<record>>
        class ValidatedSalesRequestEditDraft <<record>>
        class SalesRequestEditValidationResult <<record>>
    }

    package "Application Layer - Ordering Request" {
        class ReceivedRequestsApplicationService {
            + findRows() List
        }
        interface ReceivedRequestsPort
        class ReceivedRequestDetailApplicationService {
            + load(requestCode) ReceivedRequestDetailViewModel
            + findOrderRow(orderId) AllocatedOrderRow
        }
        interface ReceivedRequestDetailQueryPort
        class ReceivedRequestDetailViewModel <<record>>
        class ReceivedRequestDetailItemRow <<record>>
        class AllocatedOrderRow <<record>>
        class RequestRow <<record>>
    }

    package "Application Layer - Request Processing" {
        class RequestProcessingUseCase {
            + loadProcessingData(requestId) RequestProcessingData
            + validateAllocations(items, allocations) List
            + validateSubmission(items, sites, allocations, desiredDates) String
            + buildPreviewOrders(items, sites, allocations, desiredDates) List
            + createAllocatedOrders(requestId, allocations)
        }
        interface RequestProcessingGateway
        interface PreviewBuilder
        class RequestProcessingPreviewBuilder
        interface ProcessingRequestPort
    }

    package "Application Layer - Order Cancellation" {
        class OrderCancellationApplicationService {
            + cancel(orderId) CancellationResult
            + canCancel(orderId) boolean
        }
        class "OrderCancellationApplicationService.CancellationResult" as CancellationResult <<record>>
        class CancelledOrderProcessingUseCase {
            + loadProcessingData(cancelledOrderId) CancelledOrderProcessingData
            + validateSubmission(items, sites, allocations, desiredDates) String
            + buildPreviewOrders(items, sites, allocations, desiredDate) List
            + createAllocatedOrders(cancelledOrderId, allocations)
        }
        interface CancelledOrderProcessingGateway
        class CancelledOrderProcessingPreviewBuilder
    }

    package "Application Layer - Warehouse Receiving" {
        class WarehouseReceivingUseCase {
            + findInboundOrders() List
            + findItemsByOrderId(orderId) List
            + confirmArrival(orderId, items, overallNote) ConfirmationResult
        }
        class "WarehouseReceivingUseCase.ConfirmationResult" as WarehouseConfirmationResult <<record>>
        class "WarehouseReceivingUseCase.InspectionItemInput" as InspectionItemInput <<record>>
        interface WarehouseReceiptRepository
    }

    package "Infrastructure Layer" {
        class JdbcSalesRequestQueryRepository
        class JdbcSalesRequestCommandRepository
        class JdbcReceivedRequestsRepository
        class JdbcReceivedRequestDetailQuery
        class JdbcRequestProcessingGateway
        class JdbcProcessingRequestRepository
        class JdbcCancelledOrderProcessingGateway
        class JdbcOrderRepository
        class JdbcWarehouseReceiptRepository
    }

    package "Domain Layer - Request" {
        class Request {
            + addItem(merchandiseId, quantity, desiredDate)
            + startProcessing()
            + complete()
        }
        class RequestMerchandise
        enum RequestStatus
    }

    package "Domain Layer - Allocation" {
        class RequestProcessingData <<record>>
        class CancelledOrderProcessingData <<record>>
        class ItemRequirement <<record>>
        class SiteStockOption <<record>>
        class Allocation
        class AllocationPlan
        class AllocationControl {
            + applyAllocationChange(request) AllocationChangeResult
            + applyOptimalAllocation()
            + buildSuggestedPlans() List
            + applySelectedPlan(plan)
        }
        interface AllocationSuggester
        class DefaultAllocationSuggester
        interface AllocationValidator
        class DefaultAllocationValidator
        interface AllocationObjective
        class FastDeliveryObjective
        class SuggestedPlan <<record>>
        class AllocationDraft <<record>>
    }

    package "Domain Layer - Order/Warehouse" {
        class Order
        class OrderMerchandise
        enum OrderStatus
        interface OrderRepository
        class WarehouseReceipt
        class WarehouseReceiptItem
        enum InspectionResult
    }

    package "Shared Services" {
        interface InventoryRepository
        interface SiteRepository
        class SiteUseCase
        class MerchandiseUseCase
        interface MerchandiseRepository
        interface TransactionRunner
    }
}

' --- UC: Tạo yêu cầu đặt hàng ---
SalesRequestListView ..> SalesRequestCreationDialog
SalesRequestCreationDialog *-- SalesRequestCreationView
SalesRequestCreationView *-- SalesRequestCreationItemRow
SalesRequestCreationView --> SalesRequestCreationController
SalesRequestCreationController --> SalesRequestQueryService
SalesRequestCreationController --> CreateSalesRequestUseCase
CreateSalesRequestService ..|> CreateSalesRequestUseCase
CreateSalesRequestService --> SalesRequestCommandPort
CreateSalesRequestService --> InventoryRepository
CreateSalesRequestService ..> Request
CreateSalesRequestService ..> SalesRequestItemSubmission
JdbcSalesRequestCommandRepository ..|> SalesRequestCommandPort

' --- UC: Sửa yêu cầu đặt hàng ---
SalesRequestListView ..> SalesRequestEditDialogLauncher
SalesRequestEditDialog ..|> SalesRequestEditDialogLauncher
SalesRequestEditDialog --> SalesRequestEditScreenStarter
SalesRequestEditDialog *-- SalesRequestEditView
SalesRequestEditView --> SalesRequestEditActionHandler
SalesRequestEditView *-- SalesRequestEditTableComponent
SalesRequestEditTableComponent *-- SalesRequestEditItemRow
SalesRequestEditController ..|> SalesRequestEditScreenStarter
SalesRequestEditController ..|> SalesRequestEditActionHandler
SalesRequestEditController *-- SalesRequestEditSession
SalesRequestEditSession --> SalesRequestEditUseCase
SalesRequestEditSession *-- SalesRequestEditState
SalesRequestEditSession ..> SalesRequestEditViewState
SalesRequestEditUseCase --> SalesRequestEditGateway
SalesRequestEditUseCase --> SalesRequestEditMapper
SalesRequestEditUseCase --> SalesRequestEditValidator
SalesRequestEditUseCase ..> SalesRequestEditDraft
SalesRequestEditUseCase ..> ValidatedSalesRequestEditDraft
SalesRequestEditServiceGateway ..|> SalesRequestEditGateway
SalesRequestEditServiceGateway --> SalesRequestQueryService
SalesRequestEditServiceGateway --> SalesRequestCommandService
SalesRequestCommandService --> SalesRequestCommandPort
SalesRequestQueryService --> SalesRequestQueryPort
SalesRequestQueryService --> InventoryRepository
SalesRequestQueryService --> MerchandiseUseCase
SalesRequestCommandPort ..> Request
SalesRequestQueryPort ..> Request
JdbcSalesRequestQueryRepository ..|> SalesRequestQueryPort
JdbcSalesRequestQueryRepository ..> Request
JdbcSalesRequestCommandRepository ..> Request

' --- UC: Xem chi tiết yêu cầu đặt hàng đã nhận ---
ReceivedRequestsView --> Navigator
ReceivedRequestsView --> ReceivedRequestsController
ReceivedRequestsView --> RequestDetailContext
ReceivedRequestsView ..> RequestDetailPopup
ReceivedRequestsController --> ReceivedRequestsApplicationService
ReceivedRequestsApplicationService --> ReceivedRequestsPort
JdbcReceivedRequestsRepository ..|> ReceivedRequestsPort
RequestDetailContext --> RequestDetailPopupController
RequestDetailPopup *-- RequestDetailPopupView
RequestDetailPopupView --> RequestDetailContext
RequestDetailPopupView *-- RequestItemTableView
RequestDetailPopupView *-- AllocatedOrderTableView
RequestDetailPopupView ..> OrderDetailView
RequestDetailPopupController --> ReceivedRequestDetailApplicationService
ReceivedRequestDetailApplicationService --> ReceivedRequestDetailQueryPort
ReceivedRequestDetailApplicationService ..> ReceivedRequestDetailViewModel
ReceivedRequestDetailApplicationService ..> ReceivedRequestDetailItemRow
ReceivedRequestDetailApplicationService ..> AllocatedOrderRow
JdbcReceivedRequestDetailQuery ..|> ReceivedRequestDetailQueryPort
RequestDetailPopupController --> OrderCancellationApplicationService
OrderCancellationApplicationService --> OrderRepository

' --- UC: Xử lý yêu cầu đặt hàng ---
RequestProcessingLayoutView --> RequestProcessingLayoutController
RequestProcessingLayoutView *-- SiteFilterView
RequestProcessingLayoutView *-- ItemsSectionView
RequestProcessingLayoutView ..> AllSuggestPopupView
RequestProcessingLayoutView ..> RequestProcessingPreviewDialog
RequestProcessingPreviewDialog *-- RequestProcessingPreviewDialogView
RequestProcessingLayoutController *-- RequestProcessingSession
RequestProcessingLayoutController ..> RequestProcessingState
RequestProcessingLayoutController ..> ProcessingPreviewOrder
RequestProcessingSession --> RequestProcessingUseCase
RequestProcessingSession *-- AllocationControl
RequestProcessingSession ..> RequestProcessingData
RequestProcessingSession ..> SuggestedPlanState
RequestProcessingUseCase --> RequestProcessingGateway
RequestProcessingUseCase --> AllocationValidator
RequestProcessingUseCase --> AllocationSuggester
RequestProcessingUseCase ..> RequestProcessingPreviewBuilder
RequestProcessingPreviewBuilder ..|> PreviewBuilder
JdbcRequestProcessingGateway ..|> RequestProcessingGateway
JdbcRequestProcessingGateway --> ProcessingRequestPort
JdbcRequestProcessingGateway --> OrderRepository
JdbcRequestProcessingGateway --> SiteRepository
JdbcRequestProcessingGateway --> InventoryRepository
JdbcRequestProcessingGateway --> MerchandiseRepository
JdbcProcessingRequestRepository ..|> ProcessingRequestPort

' --- UC: Xử lý đơn hàng bị hủy ---
OrderCancellationLayoutView --> Navigator
OrderCancellationLayoutView --> OrderCancellationProcessingController
OrderCancellationLayoutView *-- OrderAllocationView
OrderCancellationLayoutView ..> OrderSuggestionPopupView
OrderCancellationLayoutView ..> OrderPreviewView
OrderCancellationLayoutView ..> ConfirmSubmitPopupView
OrderCancellationLayoutView ..> DetailNotificationPopupView
OrderCancellationProcessingController *-- CancelledOrderProcessingSession
OrderCancellationProcessingController ..> CancelledOrderProcessingViewModel
CancelledOrderProcessingSession --> CancelledOrderProcessingUseCase
CancelledOrderProcessingSession *-- AllocationControl
CancelledOrderProcessingSession ..> CancelledOrderProcessingData
CancelledOrderProcessingUseCase --> CancelledOrderProcessingGateway
CancelledOrderProcessingUseCase --> AllocationValidator
CancelledOrderProcessingUseCase --> AllocationSuggester
CancelledOrderProcessingUseCase ..> CancelledOrderProcessingPreviewBuilder
JdbcCancelledOrderProcessingGateway ..|> CancelledOrderProcessingGateway
JdbcCancelledOrderProcessingGateway --> OrderRepository
JdbcCancelledOrderProcessingGateway --> ProcessingRequestPort
JdbcCancelledOrderProcessingGateway --> SiteRepository
JdbcCancelledOrderProcessingGateway --> InventoryRepository
JdbcCancelledOrderProcessingGateway --> MerchandiseRepository

' --- UC: Xác nhận đơn hàng giao tới ---
ConfirmOrderArrivalView --> ConfirmOrderArrivalController
ConfirmOrderArrivalController --> WarehouseReceivingUseCase
ConfirmOrderArrivalController --> SiteUseCase
ConfirmOrderArrivalController --> MerchandiseUseCase
ConfirmOrderArrivalController ..> InspectionItemDto
ConfirmOrderArrivalController ..> WarehouseConfirmationResult
WarehouseReceivingUseCase --> OrderRepository
WarehouseReceivingUseCase --> SiteUseCase
WarehouseReceivingUseCase --> MerchandiseUseCase
WarehouseReceivingUseCase --> WarehouseReceiptRepository
WarehouseReceivingUseCase --> TransactionRunner
WarehouseReceivingUseCase ..> InspectionItemInput
WarehouseReceivingUseCase ..> WarehouseConfirmationResult
WarehouseReceivingUseCase ..> WarehouseReceipt
WarehouseReceivingUseCase ..> WarehouseReceiptItem
WarehouseReceivingUseCase ..> InspectionResult
JdbcWarehouseReceiptRepository ..|> WarehouseReceiptRepository
JdbcOrderRepository ..|> OrderRepository

' --- Domain Relations dùng chung ---
Request *-- RequestMerchandise
Request --> RequestStatus
Order *-- OrderMerchandise
Order --> OrderStatus
WarehouseReceipt *-- WarehouseReceiptItem
AllocationControl --> AllocationSuggester
AllocationControl --> AllocationPlan
AllocationControl --> Allocation
AllocationControl ..> SuggestedPlan
DefaultAllocationSuggester ..|> AllocationSuggester
DefaultAllocationSuggester --> AllocationObjective
FastDeliveryObjective ..|> AllocationObjective
DefaultAllocationValidator ..|> AllocationValidator
SuggestedPlan *-- AllocationDraft
RequestProcessingData *-- ItemRequirement
RequestProcessingData *-- SiteStockOption
CancelledOrderProcessingData *-- ItemRequirement
CancelledOrderProcessingData *-- SiteStockOption

@enduml
