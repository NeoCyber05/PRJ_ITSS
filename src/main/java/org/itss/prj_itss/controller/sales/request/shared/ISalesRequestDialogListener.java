package org.itss.prj_itss.controller.sales.request.shared;

public interface ISalesRequestDialogListener {

    void onSalesRequestSaved(SalesRequestSavedEvent event);

    default void onSalesRequestEditCancelled(int requestId) {
    }
}
