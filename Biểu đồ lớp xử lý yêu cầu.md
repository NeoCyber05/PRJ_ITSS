@startuml
package "View Layer" {
    class ReceivedRequestsView
    interface Navigator
    class RequestProcessingLayoutView
    class SiteFilterView
    class AllSuggestPopupView
    class RequestProcessingPreviewDialog
    class RequestProcessingPreviewDialogView
    class ItemsSectionView
    class SiteFilterState
}

package "Controller Layer" {
    class SiteFilterController
    class RequestProcessingPreviewDialogController
    class RequestProcessingLayoutController
    class RequestProcessingSession
    class SuggestedPlanView <<record>>
}

package Model {
    package "Application Layer" {
        class RequestProcessingUseCase
        interface RequestProcessingGateway
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
AllSuggestPopupView ..> SuggestedPlanView
RequestProcessingPreviewDialog *-- RequestProcessingPreviewDialogView
RequestProcessingPreviewDialog --> RequestProcessingPreviewDialogController
RequestProcessingPreviewDialogView --> RequestProcessingPreviewDialogController

' --- Controller Relations ---
SiteFilterController *-- SiteFilterState
RequestProcessingLayoutController *-- RequestProcessingSession
RequestProcessingSession --> RequestProcessingUseCase
RequestProcessingPreviewDialogController --> RequestProcessingLayoutController

' --- Application & Infrastructure Relations ---
RequestProcessingUseCase --> RequestProcessingGateway
RequestProcessingUseCase --> AllocationValidator
RequestProcessingUseCase --> AllocationSuggester
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
