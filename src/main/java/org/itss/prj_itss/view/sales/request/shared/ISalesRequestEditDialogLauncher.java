package org.itss.prj_itss.view.sales.request.shared;

import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.shared.ISalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;

public interface ISalesRequestEditDialogLauncher {

    void showEdit(Window owner, SalesRequestEditDialogInput input, ISalesRequestDialogListener listener);
}
