package org.itss.prj_itss.view.sales.request.update;

import org.itss.prj_itss.controller.sales.request.update.SalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;

public interface SalesRequestEditScreenStarter {

    boolean start(
            SalesRequestEditView view,
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener
    );
}
