@startuml
package "View Layer" {
    class ReceivedRequestsView
    interface Navigator
    class RequestProcessingLayoutView
    class SiteFilterView
    class SiteFilterCardView
    class AllSuggestPopupView
    class SuggestPlanCardView
    class RequestProcessingPreviewDialog
    class RequestProcessingPreviewDialogView
    class PreviewOrderCardView
    class PreviewTableRowView
    class ItemsSectionView
    class ItemsSectionItemRowView
    class AllocationItemEditorView
    class AllocationSiteRowView
}

package "Controller Layer" {
    class SiteFilterController
    class RequestProcessingPreviewDialogController
    class RequestProcessingLayoutController
    class RequestProcessingSession

    package "Request Processing State" {
        class RequestProcessingState <<record>>
        class ProcessingSiteState <<record>>
        class ProcessingItemState <<record>>
        class ProcessingPreviewOrder <<record>>
        class SuggestedPlanState <<record>>
        class SiteFilterState
        class AllocationChangeCommand <<record>>
        class AllocationChangeResult <<record>>
    }
}

package Model {
    package "Application Layer" {
        class RequestProcessingUseCase
        interface RequestProcessingGateway
        interface PreviewBuilder
        class RequestProcessingPreviewBuilder
    }

    package "Infrastructure Layer" {
        class JdbcRequestProcessingGateway
    }

    package "Domain Layer" {
        class RequestProcessingData <<record>>
        class AllocationControl
        interface AllocationSuggester
        class DefaultAllocationSuggester
        interface AllocationValidator
        class DefaultAllocationValidator
        class AllocationDraft <<record>>
        class SuggestedPlan <<record>>
        class AllocationPlan
        class ApplyPlan
        class AllocationSuggestEngine
        interface AllocationObjective
        class FastDeliveryObjective
        class AllSuggest
        class OptimalSuggest
    }
}

' --- View Relations ---
ReceivedRequestsView --> Navigator
RequestProcessingLayoutView *-- SiteFilterView
RequestProcessingLayoutView ..> ItemsSectionView
RequestProcessingLayoutView ..> AllSuggestPopupView
RequestProcessingLayoutView ..> RequestProcessingPreviewDialog
RequestProcessingLayoutView ..> RequestProcessingPreviewDialogController
RequestProcessingLayoutView --> RequestProcessingLayoutController
SiteFilterView --> SiteFilterController
AllSuggestPopupView ..> SuggestedPlanState
RequestProcessingPreviewDialog *-- RequestProcessingPreviewDialogView
RequestProcessingPreviewDialog --> RequestProcessingPreviewDialogController
RequestProcessingPreviewDialogView --> RequestProcessingPreviewDialogController

' --- View Subview Nesting (Mối quan hệ View con) ---
SiteFilterView ..> SiteFilterCardView
SiteFilterCardView --> ProcessingSiteState
AllSuggestPopupView ..> SuggestPlanCardView
SuggestPlanCardView --> SuggestedPlanState
ItemsSectionView ..> ItemsSectionItemRowView
ItemsSectionView ..> AllocationItemEditorView
ItemsSectionItemRowView --> RequestProcessingState
AllocationItemEditorView ..> AllocationSiteRowView
AllocationItemEditorView --> RequestProcessingState
AllocationSiteRowView --> AllocationChangeCommand
AllocationSiteRowView --> AllocationChangeResult
RequestProcessingPreviewDialogView ..> PreviewOrderCardView
PreviewOrderCardView ..> PreviewTableRowView
PreviewOrderCardView --> ProcessingPreviewOrder
PreviewTableRowView --> ProcessingPreviewOrder

' --- Controller Relations ---
SiteFilterController *-- SiteFilterState
RequestProcessingLayoutController *-- RequestProcessingSession
RequestProcessingSession --> RequestProcessingUseCase
RequestProcessingPreviewDialogController --> RequestProcessingLayoutController
RequestProcessingLayoutController ..> RequestProcessingState
RequestProcessingLayoutController ..> SuggestedPlanState
RequestProcessingLayoutController ..> ProcessingPreviewOrder
RequestProcessingLayoutController ..> AllocationChangeResult
RequestProcessingSession ..> RequestProcessingState
RequestProcessingSession ..> ProcessingPreviewOrder
RequestProcessingSession ..> SuggestedPlanState
RequestProcessingSession ..> AllocationChangeResult

' --- Application & Infrastructure Relations ---
RequestProcessingUseCase --> RequestProcessingGateway
RequestProcessingUseCase --> AllocationValidator
RequestProcessingUseCase --> AllocationSuggester
RequestProcessingUseCase ..> RequestProcessingPreviewBuilder
RequestProcessingPreviewBuilder ..|> PreviewBuilder
JdbcRequestProcessingGateway ..|> RequestProcessingGateway
JdbcRequestProcessingGateway ..> RequestProcessingData

' --- Domain & State Relations ---
RequestProcessingSession ..> AllocationControl : uses
RequestProcessingSession ..> RequestProcessingData
RequestProcessingSession ..> AllocationPlan
DefaultAllocationValidator ..|> AllocationValidator
DefaultAllocationSuggester ..|> AllocationSuggester
DefaultAllocationSuggester --> AllocationObjective
DefaultAllocationSuggester ..> AllSuggest
DefaultAllocationSuggester ..> OptimalSuggest
AllocationControl *-- ApplyPlan
AllocationControl --> AllocationSuggester
AllSuggest *-- AllocationSuggestEngine
OptimalSuggest ..> AllocationSuggestEngine
AllocationSuggestEngine --> AllocationObjective
FastDeliveryObjective ..|> AllocationObjective
ApplyPlan --> AllocationDraft

' --- Additional Dependency Connections ---
AllSuggest ..> SuggestedPlan
OptimalSuggest ..> SuggestedPlan
AllocationSuggestEngine ..> SuggestedPlan
@enduml
