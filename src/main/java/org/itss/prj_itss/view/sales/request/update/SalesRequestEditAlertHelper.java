package org.itss.prj_itss.view.sales.request.update;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

final class SalesRequestEditAlertHelper {

    private SalesRequestEditAlertHelper() {
    }

    static void showInfo(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}
