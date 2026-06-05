package org.itss.prj_itss.view.ordering.request;

import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.detail.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.management.OrderManagementController;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;

/**
 * Groups the dependencies needed to show a request detail popup
 * and its embedded order detail view. This avoids passing 4+ loose
 * controller/navigator parameters through multiple layers.
 */
public record RequestDetailContext(
    RequestDetailPopupController requestController,
    OrderDetailController orderController,
    OrderManagementController managementController,
    Navigator navigator
) {
}
