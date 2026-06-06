package org.itss.prj_itss.controller.sales.request.update;

public interface SalesRequestEditScreenStarter {

    boolean start(
            SalesRequestEditViewPort view,
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener
    );
}
