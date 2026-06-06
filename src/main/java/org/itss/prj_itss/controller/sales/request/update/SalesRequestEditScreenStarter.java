package org.itss.prj_itss.controller.sales.request.update;

public interface SalesRequestEditScreenStarter {

    void start(
            SalesRequestEditViewPort view,
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener
    );
}
