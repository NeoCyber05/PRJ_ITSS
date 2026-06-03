package org.itss.prj_itss.model.order;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcOrderRepository;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingSession;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingUseCase;
import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGateway;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcCancelledOrderProcessingGateway;
import org.itss.prj_itss.model.request.application.port.RequestRepository;
import org.itss.prj_itss.model.shared.database.TransactionRunner;

public final class OrderModule {

    private final OrderRepository orderRepository;
    private final OrderUseCase orderUseCase;
    private final OrderManagementApplicationService orderManagementApplicationService;
    private final OrderCancellationApplicationService orderCancellationApplicationService;

    private final ConnectionProvider connectionProvider;
    private final SiteModule siteModule;
    private final CatalogModule catalogModule;
    private CancelledOrderProcessingUseCase cancelledOrderProcessingUseCase;

    public OrderModule(ConnectionProvider connectionProvider, SiteModule siteModule, CatalogModule catalogModule) {
        this.connectionProvider = connectionProvider;
        this.siteModule = siteModule;
        this.catalogModule = catalogModule;
        this.orderRepository = new JdbcOrderRepository(connectionProvider);
        this.orderUseCase = new OrderUseCase(orderRepository);
        this.orderManagementApplicationService = new OrderManagementApplicationService(
            orderUseCase,
            siteModule.siteUseCase(),
            catalogModule.catalogUseCase()
        );
        this.orderCancellationApplicationService = new OrderCancellationApplicationService(orderUseCase);
    }

    public void initializeCancellationUseCase(RequestRepository requestRepository, TransactionRunner transactionRunner) {
        CancelledOrderProcessingGateway gateway = new JdbcCancelledOrderProcessingGateway(
            this.orderRepository,
            siteModule.siteRepository(),
            siteModule.inventoryRepository(),
            requestRepository,
            catalogModule.merchandiseRepository(),
            transactionRunner
        );
        this.cancelledOrderProcessingUseCase = new CancelledOrderProcessingUseCase(gateway);
    }

    public OrderRepository orderRepository() {
        return orderRepository;
    }

    public OrderUseCase orderUseCase() {
        return orderUseCase;
    }

    public OrderManagementApplicationService orderManagementApplicationService() {
        return orderManagementApplicationService;
    }

    public OrderCancellationApplicationService orderCancellationApplicationService() {
        return orderCancellationApplicationService;
    }

    public CancelledOrderProcessingUseCase cancelledOrderProcessingUseCase() {
        return cancelledOrderProcessingUseCase;
    }

    public CancelledOrderProcessingSession newCancellationSession() {
        if (cancelledOrderProcessingUseCase == null) {
            throw new IllegalStateException("CancelledOrderProcessingUseCase is not initialized");
        }
        return new CancelledOrderProcessingSession(cancelledOrderProcessingUseCase);
    }
}
