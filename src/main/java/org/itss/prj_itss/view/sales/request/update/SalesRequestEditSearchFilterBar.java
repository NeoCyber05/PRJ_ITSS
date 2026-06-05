package org.itss.prj_itss.view.sales.request.update;

import javafx.animation.PauseTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.function.Consumer;

final class SalesRequestEditSearchFilterBar {

    private final TextField searchField;

    SalesRequestEditSearchFilterBar(TextField searchField) {
        this.searchField = searchField;
    }

    void bind(Consumer<String> searchHandler) {
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            debounce.setOnFinished(event -> searchHandler.accept(newValue));
            debounce.playFromStart();
        });
    }

    String keyword() {
        return searchField == null ? "" : searchField.getText();
    }
}
