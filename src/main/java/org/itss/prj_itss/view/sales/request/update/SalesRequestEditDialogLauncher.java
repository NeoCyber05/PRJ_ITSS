package org.itss.prj_itss.view.sales.request.update;

import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;

public interface SalesRequestEditDialogLauncher {

    void showEdit(Window owner, SalesRequestEditDialogInput input, SalesRequestDialogListener listener);
}
