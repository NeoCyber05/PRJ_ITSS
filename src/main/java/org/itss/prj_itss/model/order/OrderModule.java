package org.itss.prj_itss.model.order;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.order.application.management.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.detail.OrderDetailApplicationService;
import org.itss.prj_itss.model.order.application.cancellation.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.application.port.SiteOrderRepository;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcOrderRepository;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingUseCase;
import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGateway;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcCancelledOrderProcessingGateway;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.shared.database.TransactionRunner;

public final class OrderModule {

    private final OrderRepository orderRepository;
    private final SiteOrderRepository siteOrderRepository;
    private final OrderDetailApplicationService orderDetailApplicationService;
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
        
        JdbcOrderRepository jdbcRepo = new JdbcOrderRepository(connectionProvider);
        this.orderRepository = jdbcRepo;
        this.siteOrderRepository = jdbcRepo;
        
        this.orderDetailApplicationService = new OrderDetailApplicationService(
            this.orderRepository,
            siteModule.siteUseCase(),
            merchandiseModule.merchandiseUseCase()
        );
        this.orderManagementApplicationService = new OrderManagementApplicationService(
            this.orderRepository,
            siteModule.siteUseCase(),
            merchandiseModule.merchandiseUseCase()
        );
        this.orderCancellationApplicationService = new OrderCancellationApplicationService(this.orderRepository);
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
        return siteOrderRepository;
    }

    public OrderManagementApplicationService orderManagementApplicationService() {
        return orderManagementApplicationService;
    }

    public OrderCancellationApplicationService orderCancellationApplicationService() {
        return orderCancellationApplicationService;
    }

    public OrderDetailApplicationService orderDetailApplicationService() {
        return orderDetailApplicationService;
    }

    public CancelledOrderProcessingUseCase cancelledOrderProcessingUseCase() {
        return cancelledOrderProcessingUseCase;
    }
}
