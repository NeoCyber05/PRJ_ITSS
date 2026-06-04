package org.itss.prj_itss.controller.sales.request.update;

public interface SalesRequestDialogListener {

    void onSalesRequestSaved(SalesRequestSavedEvent event);

    default void onSalesRequestEditCancelled(int requestId) {
    }
}
