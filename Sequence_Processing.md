# Tổng quát :
@startuml
actor "Bộ phận đặt hàng" as User #FFFFCC
boundary "ReceivedRequestsView" as RV #FFFFCC
participant "Navigator" as Nav #FFFFCC
boundary "RequestProcessingLayoutView" as LV #FFFFCC
participant "RequestProcessingLayoutController" as LC #FFFFCC
participant "RequestProcessingSession" as Session #FFFFCC
participant "RequestProcessingUseCase" as UC #FFFFCC
boundary "SiteFilterView" as FV #FFFFCC
boundary "ItemsSectionView" as IV #FFFFCC

User -> RV: Ấn nút Xử lý
RV -> Nav: showView("request-processing:" + requestId)
Nav -> LV: init(controller, navigateToView)
Nav -> LV: setRequestId(requestId)
LV -> LC: setRequestId(requestId)
LC -> Session: start(requestId)
ref over LC, Session, UC : Lấy dữ liệu yêu cầu
LV -> LV: renderProcessingScreen()
LV -> LV: renderHeader()

LV -> FV: load(sites, callback)
create FV
FV -> FC: new SiteFilterController()
create FC
FV -> FC: init(sites)
FV -> FV: renderUi()
loop For each site
    FV -> SiteFilterCardView: load(site, ...)
    create SiteFilterCardView
end

LV -> LV: renderItemsViewSection()
LV -> IV: load(vm, callbacks...)
create IV
IV -> IV: renderItems()
loop For each item
    IV -> ItemsSectionItemRowView: load(item, ...)
    create ItemsSectionItemRowView
    opt item.expanded()
        IV -> AllocationItemEditorView: load(item, ...)
        create AllocationItemEditorView
        AllocationItemEditorView -> AllocationItemEditorView: renderSiteRows()
        loop For each site row
            AllocationItemEditorView -> AllocationSiteRowView: load(siteRow, ...)
            create AllocationSiteRowView
        end
    end
end

opt Tương tác bộ lọc Site
    ref over User, FV, LC, Session, LV : Tương tác bộ lọc Site
end

alt Chọn phương án tối ưu
    ref over User, IV, LV, LC, Session : Gợi ý tối ưu
else Xem tất cả phương án
    ref over User, IV, LV, LC, Session : Hiện tất cả phương án
end

User -> LV: Ấn nút Xác nhận
LV -> LC: handleConfirm()
LC -> Session: handleConfirm()
Session -> UC: validateSubmission(...)
UC -> Session: validationMessage
alt validationMessage == null
    Session -> UC: buildPreviewOrders(items, allSites, allocations, desiredDeliveryDates)
    UC -> RequestProcessingPreviewBuilder: new RequestProcessingPreviewBuilder()
    create RequestProcessingPreviewBuilder
    UC -> RequestProcessingPreviewBuilder: items(items).sites(allSites).allocations(allocations).desiredDeliveryDates(desiredDeliveryDates).getProduct()
    RequestProcessingPreviewBuilder -> UC: List<PreviewOrder>
    UC -> Session: List<PreviewOrder>
end
Session -> LC: ConfirmResult
LC -> LV: ConfirmResult
alt validationMessage != null
    LV -> User: showValidationError(message)
else validationMessage == null
    ref over User, LV, LC, Session, UC: Xác nhận và gửi
end
@enduml



# Lấy dữ liệu yêu cầu 
@startuml
participant "RequestProcessingLayoutController" as LC #FFFFCC
participant "RequestProcessingSession" as Session #FFFFCC
participant "RequestProcessingUseCase" as UC #FFFFCC
participant "RequestProcessingGateway" as Gateway #FFFFCC

LC -> Session: start(requestId)
Session -> Session: resetProcessingState()
Session -> UC: loadProcessingData(requestId)
UC -> Gateway: loadProcessingData(requestId)
Gateway -> UC: RequestProcessingData
UC -> Session: RequestProcessingData
Session -> Session: rebuildAllocationSection()
Session -> Session: createAllocationControl()
@enduml



# Lọc site 
@startuml
actor "Bộ phận đặt hàng" as User #FFFFCC
boundary "SiteFilterView" as FV #FFFFCC
participant "SiteFilterController" as FC #FFFFCC
participant "SiteFilterState" as FS #FFFFCC
boundary "RequestProcessingLayoutView" as LV #FFFFCC
participant "RequestProcessingLayoutController" as LC #FFFFCC
participant "RequestProcessingSession" as Session #FFFFCC

User -> FV: Nhấn chọn / Loại bỏ / Bỏ ưu tiên / Tìm kiếm
alt Chọn Site (Ưu tiên)
    FV -> FC: selectSite(site)
    FC -> FS: select(site)
else Loại bỏ Site
    FV -> FC: excludeSite(site)
    FC -> FS: exclude(site)
else Tìm kiếm
    FV -> FC: search(keyword)
    FC -> FC: refreshVisibleSites()
end
FC -> FV: Cập nhật danh sách hiển thị
FV -> FV: renderUi()
FV -> LV: notifyFiltersChanged via callback
LV -> LC: handleSiteFilterChanged(excludedIds, selectedIds)
LC -> Session: handleSiteFilterChanged(excludedIds, selectedIds)
Session -> Session: AllocationPlan.using(allocations).removeSites(excludedIds)
Session -> Session: rebuildAllocationSection()
Session -> Session: createAllocationControl()
LV -> LV: renderItemsViewSection()
@enduml



# Gợi ý tối ưu 
@startuml
actor "Bộ phận đặt hàng" as User #FFFFCC
boundary "ItemsSectionView" as IV #FFFFCC
boundary "RequestProcessingLayoutView" as LV #FFFFCC
participant "RequestProcessingLayoutController" as LC #FFFFCC
participant "RequestProcessingSession" as Session #FFFFCC
participant "AllocationControl" as AC #FFFFCC
participant "AllocationSuggester" as Suggester #FFFFCC
participant "OptimalSuggest" as OptSuggest #FFFFCC
participant "AllocationSuggestEngine" as Engine #FFFFCC
participant "ApplyPlan" as ApplyPlan #FFFFCC

User -> IV: Bấm Gợi ý tối ưu
IV -> LV: handleOptimizeAllocation via callback
LV -> LC: handleOptimizeAllocation()
LC -> Session: handleOptimizeAllocation()
Session -> AC: applyOptimalAllocation()
AC -> Suggester: buildOptimalDrafts(...)
create OptSuggest
Suggester -> OptSuggest: buildOptimalDrafts(items)
create Engine
OptSuggest -> Engine: suggestMany(1, 12)
Engine -> OptSuggest: List<SuggestedPlan>
OptSuggest -> Suggester: Map<Integer, Map<Integer, AllocationDraft>>
Suggester -> AC: Map<Integer, Map<Integer, AllocationDraft>>
AC -> ApplyPlan: apply(drafts)
Session -> LV: Cập nhật giao diện
LV -> LV: renderItemsViewSection()
@enduml



# Hiện tất cả phương án 
@startuml
actor "Bộ phận đặt hàng" as User #FFFFCC
boundary "ItemsSectionView" as IV #FFFFCC
boundary "RequestProcessingLayoutView" as LV #FFFFCC
participant "RequestProcessingLayoutController" as LC #FFFFCC
participant "RequestProcessingSession" as Session #FFFFCC
participant "AllocationControl" as AC #FFFFCC
participant "AllocationSuggester" as Suggester #FFFFCC
participant "AllSuggest" as AllSuggest #FFFFCC
participant "AllocationSuggestEngine" as Engine #FFFFCC
boundary "AllSuggestPopupView" as Popup #FFFFCC
participant "ApplyPlan" as ApplyPlan #FFFFCC

User -> IV: Bấm Xem tất cả phương án
IV -> LV: handleShowAllPlans via callback
LV -> LC: handleShowAllPlans()
LC -> Session: handleShowAllPlans()
Session -> AC: buildSuggestedPlans()
AC -> Suggester: buildSuggestedPlans(...)
create AllSuggest
Suggester -> AllSuggest: buildSuggestedPlans(...)
create Engine
AllSuggest -> Engine: suggestMany(limit, maxItemVariants)
Engine -> AllSuggest: List<SuggestedPlan>
AllSuggest -> Suggester: List<SuggestedPlan>
Suggester -> AC: List<SuggestedPlan>
AC -> Session: List<SuggestedPlan>
Session -> LC: List<SuggestedPlanView> (lưu suggested plans gốc)
LC -> LV: List<SuggestedPlanView>
LV -> Popup: show(suggestedPlans, callback)
create Popup
Popup -> Popup: renderPlans()
loop For each suggested plan
    Popup -> SuggestPlanCardView: load(plan, ...)
    create SuggestPlanCardView
end
User -> Popup: Chọn 1 phương án
Popup -> LV: applySelectedPlan(plan) via callback
LV -> LC: applySelectedPlan(signature)
LC -> Session: applySelectedPlanBySignature(signature)
Session -> AC: applySelectedPlan(plan) (tìm từ cache)
AC -> ApplyPlan: apply(plan.allocationsByItem())
Session -> LV: Cập nhật giao diện
LV -> LV: renderItemsViewSection()
@enduml



# Xác nhận và gửi 
@startuml
actor "Bộ phận đặt hàng" as User #FFFFCC
boundary "RequestProcessingLayoutView" as LV #FFFFCC
participant "RequestProcessingLayoutController" as LC #FFFFCC
participant "RequestProcessingSession" as Session #FFFFCC
boundary "RequestProcessingPreviewDialog" as Dialog #FFFFCC
boundary "RequestProcessingPreviewDialogView" as DialogView #FFFFCC
participant "RequestProcessingPreviewDialogController" as DialogController #FFFFCC
participant "RequestProcessingUseCase" as UC #FFFFCC
participant "RequestProcessingGateway" as Gateway #FFFFCC
participant "Navigator" as Nav #FFFFCC

LV -> LV: showPreviewDialog(previewOrders)
LV -> DialogController: new RequestProcessingPreviewDialogController(controller, previewOrders)
create DialogController
LV -> Dialog: new RequestProcessingPreviewDialog(onOrdersRequested, DialogController)
create Dialog
LV -> Dialog: show(itemsTableContainer)
Dialog -> Dialog: loadRoot(dialog)
create DialogView
Dialog -> DialogView: init(dialog, onOrdersRequested, controller)
DialogView -> DialogController: previewOrders()
DialogController -> DialogView: List<ProcessingPreviewOrderView>
DialogView -> DialogView: render(previewOrders)
loop For each preview order
    DialogView -> PreviewOrderCardView: load(order, ...)
    create PreviewOrderCardView
    PreviewOrderCardView -> PreviewOrderCardView: init(order)
    loop For each preview line
        PreviewOrderCardView -> PreviewTableRowView: load(line)
        create PreviewTableRowView
    end
end
User -> DialogView: Ấn nút Gửi
DialogView -> DialogController: submit()
DialogController -> LC: submitAllocatedOrders()
LC -> Session: submitAllocatedOrders()
Session -> UC: createAllocatedOrders(requestId, allocations)
UC -> Gateway: createAllocatedOrders(requestId, allocations)
Gateway -> UC: void / Exception
UC -> Session: void / Exception
Session -> LC: void / Exception
LC -> DialogController: Success / Failure
DialogController -> DialogView: SubmitResult
alt Thành công
    DialogView -> DialogView: close dialog
    DialogView -> Nav: onOrdersRequested.run() (Chuyển sang tab đơn hàng)
    DialogView -> User: Hiển thị toast thành công
else Thất bại
    DialogView -> User: Hiển thị alert thông báo lỗi
end
@enduml
