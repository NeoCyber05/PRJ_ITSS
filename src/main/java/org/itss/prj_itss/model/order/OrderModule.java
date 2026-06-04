package org.itss.prj_itss.model.order;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.OrderDetailApplicationService;
import org.itss.prj_itss.model.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcOrderRepository;
import org.itss.prj_itss.model.site.SiteModule;

public final class OrderModule {

    private final OrderRepository orderRepository;
    private final OrderUseCase orderUseCase;
    private final OrderDetailApplicationService orderDetailApplicationService;
    private final OrderManagementApplicationService orderManagementApplicationService;
    private final OrderCancellationApplicationService orderCancellationApplicationService;

    public OrderModule(ConnectionProvider connectionProvider, SiteModule siteModule, CatalogModule catalogModule) {
        this.orderRepository = new JdbcOrderRepository(connectionProvider);
        this.orderUseCase = new OrderUseCase(orderRepository);
        this.orderDetailApplicationService = new OrderDetailApplicationService(
            orderUseCase,
            siteModule.siteUseCase(),
            catalogModule.catalogUseCase()
        );
        this.orderManagementApplicationService = new OrderManagementApplicationService(
            orderUseCase,
            siteModule.siteUseCase(),
            catalogModule.catalogUseCase()
        );
        this.orderCancellationApplicationService = new OrderCancellationApplicationService(orderUseCase);
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

    public OrderDetailApplicationService orderDetailApplicationService() {
        return orderDetailApplicationService;
    }
}
