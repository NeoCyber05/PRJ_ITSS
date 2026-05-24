package org.itss.prj_itss.model.order;

import org.itss.prj_itss.common.config.SharedInfrastructure;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.infrastructure.persistence.JdbcOrderRepository;
import org.itss.prj_itss.model.site.SiteModule;

public final class OrderModule {

    private final OrderRepository orderRepository;
    private final OrderUseCase orderUseCase;
    private final OrderManagementApplicationService orderManagementApplicationService;
    private final OrderCancellationApplicationService orderCancellationApplicationService;

    public OrderModule(SharedInfrastructure infrastructure, SiteModule siteModule, CatalogModule catalogModule) {
        this.orderRepository = new JdbcOrderRepository(infrastructure.connectionProvider());
        this.orderUseCase = new OrderUseCase(orderRepository);
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
}
