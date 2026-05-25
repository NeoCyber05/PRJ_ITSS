package org.itss.prj_itss.controller.sales.request.shared;

public interface OrderRequestDialogListener {

    void onOrderRequestUpdated(OrderRequestUpdatedEvent event);

    default void onOrderRequestCancelled(int requestId) {
    }
}
