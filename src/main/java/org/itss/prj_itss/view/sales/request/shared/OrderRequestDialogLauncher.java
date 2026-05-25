package org.itss.prj_itss.view.sales.request.shared;

import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.shared.OrderRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.UpdateOrderRequestDialogInput;

public interface OrderRequestDialogLauncher {

    void showUpdate(Window owner, UpdateOrderRequestDialogInput input, OrderRequestDialogListener listener);
}
