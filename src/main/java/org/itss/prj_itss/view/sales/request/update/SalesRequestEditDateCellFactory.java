package org.itss.prj_itss.view.sales.request.update;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

import java.time.LocalDate;

final class SalesRequestEditDateCellFactory {

    private SalesRequestEditDateCellFactory() {
    }

    static Callback<DatePicker, DateCell> disablePastDates(DatePicker datePicker) {
        return picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    return;
                }

                LocalDate today = LocalDate.now();
                if (date.isBefore(today)) {
                    // UX guard only. SalesRequestEditValidator remains the final business rule authority.
                    setDisable(true);
                    setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-border-color: transparent;");
                } else if (datePicker.getValue() != null && date.equals(datePicker.getValue())) {
                    setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #1e3a8a; -fx-font-weight: bold; -fx-border-color: transparent;");
                } else if (date.equals(today)) {
                    setStyle("-fx-border-color: transparent;");
                } else {
                    setStyle("");
                }
            }
        };
    }
}
