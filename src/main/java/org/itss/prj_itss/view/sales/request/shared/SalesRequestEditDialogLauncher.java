package org.itss.prj_itss.view.sales.request.shared;

import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.shared.SalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;

public interface SalesRequestEditDialogLauncher {

    void showEdit(Window owner, SalesRequestEditDialogInput input, SalesRequestDialogListener listener);
}
