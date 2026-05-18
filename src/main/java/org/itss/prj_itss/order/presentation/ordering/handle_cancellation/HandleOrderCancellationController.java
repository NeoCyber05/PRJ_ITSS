package org.itss.prj_itss.order.presentation.ordering.handle_cancellation;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.order.application.OrderCancellationApplicationService;

public final class HandleOrderCancellationController implements IViewController {

    private OrderCancellationApplicationService orderCancellationApplicationService;

    @Override
    public void init(INavigator navigator, ApplicationContext context) {
        this.orderCancellationApplicationService = context.orderCancellationApplicationService();
    }

    public OrderCancellationApplicationService orderCancellationApplicationService() {
        return orderCancellationApplicationService;
    }
}
