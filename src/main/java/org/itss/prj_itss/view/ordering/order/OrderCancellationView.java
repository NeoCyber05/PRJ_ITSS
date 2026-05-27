package org.itss.prj_itss.view.ordering.order;

import javafx.fxml.FXML;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderCancellationController;
import org.itss.prj_itss.view.shared.ViewLifecycle;

public final class OrderCancellationView implements ViewLifecycle {

    private Navigator navigator;
    private OrderCancellationController controller;

    @FXML
    private void initialize() {
    }

    public void init(Navigator navigator, OrderCancellationController controller) {
        this.navigator = navigator;
        this.controller = controller;
    }
}
