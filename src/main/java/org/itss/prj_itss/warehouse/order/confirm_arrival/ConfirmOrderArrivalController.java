package org.itss.prj_itss.warehouse.order.confirm_arrival;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;

/**
 * Controller skeleton cho UC "Xác nhận đơn hàng giao tới" (Warehouse).
 */
public final class ConfirmOrderArrivalController implements IViewController {

    private ConfirmOrderArrivalService confirmOrderArrivalService;

    @Override
    public void init(INavigator navigator, ApplicationContext context) {
        this.confirmOrderArrivalService = new ConfirmOrderArrivalService(context.orderService());
    }

    public ConfirmOrderArrivalService service() {
        return confirmOrderArrivalService;
    }
}
