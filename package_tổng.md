@startuml
title Biểu đồ phụ thuộc package cho các UC yêu cầu đặt hàng

left to right direction
skinparam packageStyle rectangle
skinparam shadowing false

legend right
Chiều mũi tên: package nguồn gọi/import package đích.
Sơ đồ gom theo các UC trong class_tổng.md:
- Sales: tạo/sửa/xem yêu cầu đặt hàng
- Ordering: xem/xử lý yêu cầu, xử lý đơn hàng bị hủy
- Warehouse: xác nhận đơn hàng giao tới
endlegend

package "View Layer" {
    package "view.ordering" as V_ORDERING {
        package "view.ordering.request" as V_ORDERING_REQUEST {
            package "detail" as V_ORDERING_REQUEST_DETAIL
            package "process" as V_ORDERING_REQUEST_PROCESS {
                package "layout" as V_ORDERING_REQUEST_PROCESS_LAYOUT
                package "site" as V_ORDERING_REQUEST_PROCESS_SITE
                package "items" as V_ORDERING_REQUEST_PROCESS_ITEMS
                package "preview" as V_ORDERING_REQUEST_PROCESS_PREVIEW
                package "suggest" as V_ORDERING_REQUEST_PROCESS_SUGGEST
                package "state" as V_ORDERING_REQUEST_PROCESS_STATE
            }
        }

        package "view.ordering.order" as V_ORDERING_ORDER {
            package "detail" as V_ORDERING_ORDER_DETAIL
            package "management" as V_ORDERING_ORDER_MANAGEMENT
            package "cancellation" as V_ORDERING_ORDER_CANCELLATION {
                package "allocation" as V_ORDERING_ORDER_CANCELLATION_ALLOCATION
                package "popup" as V_ORDERING_ORDER_CANCELLATION_POPUP
                package "preview" as V_ORDERING_ORDER_CANCELLATION_PREVIEW
                package "suggestion" as V_ORDERING_ORDER_CANCELLATION_SUGGESTION
            }
        }
    }

    package "view.sales" as V_SALES {
        package "view.sales.request" as V_SALES_REQUEST {
            package "list" as V_SALES_REQUEST_LIST
            package "create" as V_SALES_REQUEST_CREATE
            package "update" as V_SALES_REQUEST_UPDATE
            package "view" as V_SALES_REQUEST_VIEW
        }
    }

    package "view.warehouse" as V_WAREHOUSE
    package "view.layout" as V_LAYOUT
    package "view.shared.ui" as V_SHARED_UI
}

package "Controller Layer" {
    package "controller.ordering" as C_ORDERING {
        package "controller.ordering.request" as C_ORDERING_REQUEST {
            package "process" as C_ORDERING_REQUEST_PROCESS {
                package "site" as C_ORDERING_REQUEST_PROCESS_SITE
                package "preview" as C_ORDERING_REQUEST_PROCESS_PREVIEW
                package "session" as C_ORDERING_REQUEST_PROCESS_SESSION
                package "state" as C_ORDERING_REQUEST_PROCESS_STATE
            }
        }

        package "controller.ordering.order" as C_ORDERING_ORDER {
            package "detail" as C_ORDERING_ORDER_DETAIL
            package "management" as C_ORDERING_ORDER_MANAGEMENT
            package "cancellation" as C_ORDERING_ORDER_CANCELLATION {
                package "state" as C_ORDERING_ORDER_CANCELLATION_STATE
            }
        }
    }

    package "controller.sales" as C_SALES {
        package "controller.sales.request" as C_SALES_REQUEST {
            package "list" as C_SALES_REQUEST_LIST
            package "create" as C_SALES_REQUEST_CREATE
            package "update" as C_SALES_REQUEST_UPDATE {
                package "session" as C_SALES_REQUEST_UPDATE_SESSION
            }
            package "view" as C_SALES_REQUEST_VIEW
        }
    }

    package "controller.warehouse" as C_WAREHOUSE
    package "controller.navigation" as C_NAVIGATION
    package "controller.shared" as C_SHARED
}

package "Model Layer" {
    package "model.request" as M_REQUEST {
        package "application" as M_REQUEST_APP {
            package "sales" as M_REQUEST_APP_SALES {
                package "create" as M_REQUEST_APP_SALES_CREATE
                package "update" as M_REQUEST_APP_SALES_UPDATE
                package "shared" as M_REQUEST_APP_SALES_SHARED
                package "view" as M_REQUEST_APP_SALES_VIEW
            }
            package "listing" as M_REQUEST_APP_LISTING
            package "international.detail" as M_REQUEST_APP_DETAIL
            package "processing" as M_REQUEST_APP_PROCESSING
        }

        package "domain" as M_REQUEST_DOMAIN {
            package "request" as M_REQUEST_DOMAIN_REQUEST
            package "processing" as M_REQUEST_DOMAIN_PROCESSING {
                package "allocation" as M_REQUEST_DOMAIN_ALLOCATION {
                    package "policy" as M_REQUEST_DOMAIN_ALLOCATION_POLICY
                    package "validator" as M_REQUEST_DOMAIN_ALLOCATION_VALIDATOR
                }
                package "suggestion" as M_REQUEST_DOMAIN_SUGGESTION {
                    package "algo" as M_REQUEST_DOMAIN_SUGGESTION_ALGO
                }
            }
            package "delivery" as M_REQUEST_DOMAIN_DELIVERY
        }

        package "infrastructure.persistence" as M_REQUEST_INFRA
    }

    package "model.order" as M_ORDER {
        package "application" as M_ORDER_APP {
            package "cancellation" as M_ORDER_APP_CANCELLATION
            package "detail" as M_ORDER_APP_DETAIL
            package "management" as M_ORDER_APP_MANAGEMENT
            package "port" as M_ORDER_APP_PORT
        }
        package "domain" as M_ORDER_DOMAIN {
            package "cancellation" as M_ORDER_DOMAIN_CANCELLATION
        }
        package "infrastructure.persistence" as M_ORDER_INFRA
    }

    package "model.warehouse" as M_WAREHOUSE {
        package "application" as M_WAREHOUSE_APP {
            package "port" as M_WAREHOUSE_APP_PORT
        }
        package "domain" as M_WAREHOUSE_DOMAIN
        package "infrastructure.persistence" as M_WAREHOUSE_INFRA
    }

    package "model.site" as M_SITE {
        package "application" as M_SITE_APP {
            package "port" as M_SITE_APP_PORT
        }
        package "domain" as M_SITE_DOMAIN
        package "infrastructure.persistence" as M_SITE_INFRA
    }

    package "model.merchandise" as M_MERCHANDISE {
        package "application" as M_MERCHANDISE_APP {
            package "port" as M_MERCHANDISE_APP_PORT
        }
        package "domain" as M_MERCHANDISE_DOMAIN
        package "infrastructure.persistence" as M_MERCHANDISE_INFRA
    }

    package "model.shared" as M_SHARED {
        package "database" as M_SHARED_DB
        package "formatting" as M_SHARED_FORMATTING
    }

    package "model.auth" as M_AUTH {
        package "domain" as M_AUTH_DOMAIN
    }
}

' --- View -> Controller ---
V_SALES_REQUEST_LIST --> C_SALES_REQUEST_LIST
V_SALES_REQUEST_CREATE --> C_SALES_REQUEST_CREATE
V_SALES_REQUEST_UPDATE --> C_SALES_REQUEST_UPDATE
V_SALES_REQUEST_VIEW --> C_SALES_REQUEST_VIEW
V_SALES_REQUEST --> V_SHARED_UI

V_ORDERING_REQUEST --> C_ORDERING_REQUEST
V_ORDERING_REQUEST --> C_NAVIGATION
V_ORDERING_REQUEST_DETAIL --> C_ORDERING_REQUEST
V_ORDERING_REQUEST_DETAIL --> C_ORDERING_ORDER_DETAIL
V_ORDERING_REQUEST_DETAIL --> C_ORDERING_ORDER_MANAGEMENT
V_ORDERING_REQUEST_PROCESS --> C_ORDERING_REQUEST_PROCESS
V_ORDERING_REQUEST_PROCESS --> C_NAVIGATION
V_ORDERING_REQUEST_PROCESS --> V_SHARED_UI

V_ORDERING_ORDER_DETAIL --> C_ORDERING_ORDER_DETAIL
V_ORDERING_ORDER_MANAGEMENT --> C_ORDERING_ORDER_MANAGEMENT
V_ORDERING_ORDER_CANCELLATION --> C_ORDERING_ORDER_CANCELLATION
V_ORDERING_ORDER_CANCELLATION --> C_NAVIGATION
V_ORDERING_ORDER_CANCELLATION --> V_ORDERING_REQUEST_PROCESS_STATE

V_WAREHOUSE --> C_WAREHOUSE
V_WAREHOUSE --> V_SHARED_UI
V_LAYOUT --> C_NAVIGATION

' --- View -> Model DTO/ViewModel dependencies that exist in this repo ---
V_SALES_REQUEST_CREATE --> M_REQUEST_APP_SALES_SHARED
V_SALES_REQUEST_UPDATE --> M_REQUEST_APP_SALES_SHARED
V_SALES_REQUEST_UPDATE --> C_SALES_REQUEST_UPDATE
V_ORDERING_REQUEST --> M_REQUEST_APP_LISTING
V_ORDERING_REQUEST_DETAIL --> M_REQUEST_APP_DETAIL
V_ORDERING_ORDER_DETAIL --> M_ORDER_APP_DETAIL
V_ORDERING_ORDER_MANAGEMENT --> M_ORDER_APP_MANAGEMENT
V_WAREHOUSE --> M_ORDER_DOMAIN
V_WAREHOUSE --> M_SITE_DOMAIN
V_WAREHOUSE --> M_WAREHOUSE_APP
V_WAREHOUSE --> M_WAREHOUSE_DOMAIN

' --- Controller -> Model ---
C_SALES_REQUEST_LIST --> M_REQUEST_APP_LISTING
C_SALES_REQUEST_CREATE --> M_REQUEST_APP_SALES
C_SALES_REQUEST_CREATE --> M_REQUEST_APP_SALES_CREATE
C_SALES_REQUEST_CREATE --> M_REQUEST_APP_SALES_SHARED
C_SALES_REQUEST_UPDATE --> M_REQUEST_APP_SALES
C_SALES_REQUEST_UPDATE --> M_REQUEST_APP_SALES_UPDATE
C_SALES_REQUEST_UPDATE --> M_REQUEST_APP_SALES_SHARED
C_SALES_REQUEST_UPDATE_SESSION --> M_REQUEST_APP_SALES_UPDATE
C_SALES_REQUEST_VIEW --> M_REQUEST_APP_SALES_VIEW
C_SALES_REQUEST --> C_SHARED

C_ORDERING_REQUEST --> M_REQUEST_APP_LISTING
C_ORDERING_REQUEST --> M_REQUEST_APP_DETAIL
C_ORDERING_REQUEST --> M_ORDER_APP_CANCELLATION
C_ORDERING_REQUEST_PROCESS --> M_REQUEST_APP_PROCESSING
C_ORDERING_REQUEST_PROCESS_SESSION --> M_REQUEST_DOMAIN_PROCESSING
C_ORDERING_REQUEST_PROCESS_SESSION --> M_REQUEST_DOMAIN_ALLOCATION
C_ORDERING_REQUEST_PROCESS_STATE --> M_REQUEST_DOMAIN_PROCESSING
C_ORDERING_ORDER_DETAIL --> M_ORDER_APP_DETAIL
C_ORDERING_ORDER_MANAGEMENT --> M_ORDER_APP_MANAGEMENT
C_ORDERING_ORDER_CANCELLATION --> M_ORDER_APP_CANCELLATION
C_ORDERING_ORDER_CANCELLATION_STATE --> M_ORDER_APP_CANCELLATION
C_ORDERING_ORDER_CANCELLATION_STATE --> M_REQUEST_DOMAIN_PROCESSING
C_ORDERING_ORDER_CANCELLATION_STATE --> M_REQUEST_DOMAIN_ALLOCATION

C_WAREHOUSE --> M_WAREHOUSE_APP
C_WAREHOUSE --> M_ORDER_DOMAIN
C_WAREHOUSE --> M_SITE_APP
C_WAREHOUSE --> M_SITE_DOMAIN
C_WAREHOUSE --> M_MERCHANDISE_APP
C_WAREHOUSE --> M_MERCHANDISE_DOMAIN
C_WAREHOUSE --> M_WAREHOUSE_DOMAIN
C_WAREHOUSE --> C_SHARED

' --- Model application/domain dependencies ---
M_REQUEST_APP_SALES_CREATE --> M_REQUEST_DOMAIN_REQUEST
M_REQUEST_APP_SALES_CREATE --> M_REQUEST_APP_SALES_SHARED
M_REQUEST_APP_SALES_CREATE --> M_SITE_APP_PORT
M_REQUEST_APP_SALES --> M_REQUEST_DOMAIN_REQUEST
M_REQUEST_APP_SALES --> M_REQUEST_APP_SALES_SHARED
M_REQUEST_APP_SALES --> M_SITE_APP_PORT
M_REQUEST_APP_SALES --> M_MERCHANDISE_APP
M_REQUEST_APP_SALES_UPDATE --> M_REQUEST_APP_SALES
M_REQUEST_APP_SALES_UPDATE --> M_REQUEST_APP_SALES_SHARED
M_REQUEST_APP_SALES_UPDATE --> M_REQUEST_DOMAIN_REQUEST
M_REQUEST_APP_DETAIL --> M_SHARED_FORMATTING
M_REQUEST_APP_LISTING --> M_REQUEST_DOMAIN_REQUEST
M_REQUEST_APP_PROCESSING --> M_REQUEST_DOMAIN_PROCESSING
M_REQUEST_APP_PROCESSING --> M_REQUEST_DOMAIN_ALLOCATION
M_REQUEST_APP_PROCESSING --> M_REQUEST_DOMAIN_SUGGESTION

M_REQUEST_DOMAIN_ALLOCATION --> M_REQUEST_DOMAIN_DELIVERY
M_REQUEST_DOMAIN_ALLOCATION --> M_REQUEST_DOMAIN_SUGGESTION
M_REQUEST_DOMAIN_SUGGESTION --> M_REQUEST_DOMAIN_PROCESSING
M_REQUEST_DOMAIN_SUGGESTION --> M_REQUEST_DOMAIN_ALLOCATION_POLICY
M_REQUEST_DOMAIN_SUGGESTION_ALGO --> M_REQUEST_DOMAIN_PROCESSING
M_REQUEST_DOMAIN_SUGGESTION_ALGO --> M_REQUEST_DOMAIN_ALLOCATION

M_ORDER_APP_CANCELLATION --> M_ORDER_APP_PORT
M_ORDER_APP_CANCELLATION --> M_ORDER_DOMAIN
M_ORDER_APP_CANCELLATION --> M_ORDER_DOMAIN_CANCELLATION
M_ORDER_APP_CANCELLATION --> M_REQUEST_DOMAIN_PROCESSING
M_ORDER_APP_CANCELLATION --> M_REQUEST_DOMAIN_ALLOCATION
M_ORDER_APP_DETAIL --> M_ORDER_APP_PORT
M_ORDER_APP_MANAGEMENT --> M_ORDER_APP_PORT

M_WAREHOUSE_APP --> M_ORDER_APP_PORT
M_WAREHOUSE_APP --> M_ORDER_DOMAIN
M_WAREHOUSE_APP --> M_SITE_APP
M_WAREHOUSE_APP --> M_MERCHANDISE_APP
M_WAREHOUSE_APP --> M_WAREHOUSE_DOMAIN
M_WAREHOUSE_APP --> M_WAREHOUSE_APP_PORT
M_WAREHOUSE_APP --> M_SHARED_DB
M_WAREHOUSE_APP --> M_AUTH_DOMAIN

' --- Infrastructure implements/uses application ports ---
M_REQUEST_INFRA ..> M_REQUEST_APP_SALES
M_REQUEST_INFRA ..> M_REQUEST_APP_LISTING
M_REQUEST_INFRA ..> M_REQUEST_APP_DETAIL
M_REQUEST_INFRA ..> M_REQUEST_APP_PROCESSING
M_REQUEST_INFRA --> M_REQUEST_DOMAIN_REQUEST
M_REQUEST_INFRA --> M_REQUEST_DOMAIN_PROCESSING
M_REQUEST_INFRA --> M_ORDER_APP_PORT
M_REQUEST_INFRA --> M_SITE_APP_PORT
M_REQUEST_INFRA --> M_MERCHANDISE_APP_PORT
M_REQUEST_INFRA --> M_SHARED_DB

M_ORDER_INFRA ..> M_ORDER_APP_PORT
M_ORDER_INFRA ..> M_ORDER_APP_CANCELLATION
M_ORDER_INFRA --> M_ORDER_DOMAIN
M_ORDER_INFRA --> M_ORDER_DOMAIN_CANCELLATION
M_ORDER_INFRA --> M_REQUEST_APP_PROCESSING
M_ORDER_INFRA --> M_REQUEST_DOMAIN_REQUEST
M_ORDER_INFRA --> M_REQUEST_DOMAIN_PROCESSING
M_ORDER_INFRA --> M_SITE_APP_PORT
M_ORDER_INFRA --> M_MERCHANDISE_APP_PORT
M_ORDER_INFRA --> M_SHARED_DB

M_WAREHOUSE_INFRA ..> M_WAREHOUSE_APP_PORT
M_WAREHOUSE_INFRA --> M_WAREHOUSE_DOMAIN
M_WAREHOUSE_INFRA --> M_SHARED_DB

M_SITE_INFRA ..> M_SITE_APP_PORT
M_SITE_INFRA --> M_SITE_DOMAIN
M_SITE_INFRA --> M_SHARED_DB

M_MERCHANDISE_INFRA ..> M_MERCHANDISE_APP_PORT
M_MERCHANDISE_INFRA --> M_MERCHANDISE_DOMAIN
M_MERCHANDISE_INFRA --> M_SHARED_DB

note right of V_ORDERING_REQUEST_PROCESS_STATE
Package state trong view đang là presentation-state dùng chung.
Trong chuẩn MVC nghiêm ngặt, đây là điểm nên gom về controller/presenter.
end note

note bottom of M_REQUEST_INFRA
Infrastructure phụ thuộc application ports và domain,
nhưng application/domain không phụ thuộc ngược infrastructure.
end note

@enduml
