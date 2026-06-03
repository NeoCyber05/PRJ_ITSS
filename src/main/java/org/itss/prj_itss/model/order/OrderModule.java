package org.itss.prj_itss.model.order;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.application.port.SiteOrderRepository;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcOrderRepository;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingSession;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingUseCase;
import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGateway;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcCancelledOrderProcessingGateway;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.shared.database.TransactionRunner;

public final class OrderModule {

    private final OrderRepository orderRepository;
    private final OrderUseCase orderUseCase;
    private final OrderManagementApplicationService orderManagementApplicationService;
    private final OrderCancellationApplicationService orderCancellationApplicationService;

    private final ConnectionProvider connectionProvider;
    private final SiteModule siteModule;
    private final MerchandiseModule merchandiseModule;
    private CancelledOrderProcessingUseCase cancelledOrderProcessingUseCase;

    public OrderModule(ConnectionProvider connectionProvider, SiteModule siteModule, MerchandiseModule merchandiseModule) {
        this.connectionProvider = connectionProvider;
        this.siteModule = siteModule;
        this.merchandiseModule = merchandiseModule;
        this.orderRepository = new JdbcOrderRepository(connectionProvider);
        this.orderUseCase = new OrderUseCase(orderRepository);
        this.orderManagementApplicationService = new OrderManagementApplicationService(
            orderUseCase,
            siteModule.siteUseCase(),
            merchandiseModule.merchandiseUseCase()
        );
        this.orderCancellationApplicationService = new OrderCancellationApplicationService(orderUseCase);
    }

    public void initializeCancellationUseCase(ProcessingRequestPort requestRepository, TransactionRunner transactionRunner) {
        CancelledOrderProcessingGateway gateway = new JdbcCancelledOrderProcessingGateway(
            this.orderRepository,
            siteModule.siteRepository(),
            siteModule.inventoryRepository(),
            requestRepository,
            merchandiseModule.merchandiseRepository(),
            transactionRunner
        );
        this.cancelledOrderProcessingUseCase = new CancelledOrderProcessingUseCase(gateway);
    }

    public OrderRepository orderRepository() {
        return orderRepository;
    }

    public SiteOrderRepository siteOrderRepository() {
        return (SiteOrderRepository) orderRepository;
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
