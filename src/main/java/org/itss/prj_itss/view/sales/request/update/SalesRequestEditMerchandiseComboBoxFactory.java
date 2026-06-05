package org.itss.prj_itss.view.sales.request.update;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

final class SalesRequestEditMerchandiseComboBoxFactory {

    private SalesRequestEditMerchandiseComboBoxFactory() {
    }

    static ComboBox<MerchandiseOption> create(
            boolean useCode,
            List<MerchandiseOption> availableOptions,
            Consumer<MerchandiseOption> selectionHandler
    ) {
        ComboBox<MerchandiseOption> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.setPrefWidth(useCode ? 110 : 240);

        ObservableList<MerchandiseOption> baseOptions = FXCollections.observableArrayList(availableOptions);
        comboBox.setItems(baseOptions);
        comboBox.setConverter(converter(comboBox, useCode));
        comboBox.valueProperty().addListener((obs, oldValue, newValue) -> selectionHandler.accept(newValue));
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) ->
            filterOptions(comboBox, baseOptions, newValue, useCode)
        );
        comboBox.setOnShowing(event -> {
            if (comboBox.getEditor().getText() == null || comboBox.getEditor().getText().isBlank()) {
                comboBox.setItems(baseOptions);
            }
        });
        return comboBox;
    }

    private static StringConverter<MerchandiseOption> converter(
            ComboBox<MerchandiseOption> comboBox,
            boolean useCode
    ) {
        return new StringConverter<>() {
            @Override
            public String toString(MerchandiseOption merchandise) {
                return merchandise == null ? "" : labelOf(merchandise, useCode);
            }

            @Override
            public MerchandiseOption fromString(String text) {
                return comboBox.getItems().stream()
                    .filter(merchandise -> labelOf(merchandise, useCode).equals(text))
                    .findFirst()
                    .orElse(null);
            }
        };
    }

    private static void filterOptions(
            ComboBox<MerchandiseOption> comboBox,
            ObservableList<MerchandiseOption> availableOptions,
            String text,
            boolean useCode
    ) {
        MerchandiseOption selected = comboBox.getSelectionModel().getSelectedItem();
        if (selected != null && labelOf(selected, useCode).equals(text)) {
            return;
        }

        if (text == null || text.isBlank()) {
            comboBox.setItems(availableOptions);
        } else {
            String lower = text.toLowerCase(Locale.ROOT);
            comboBox.setItems(FXCollections.observableArrayList(
                availableOptions.stream()
                    .filter(merchandise -> SalesRequestEditViewSupport.contains(labelOf(merchandise, useCode), lower))
                    .toList()
            ));
        }

        if (comboBox.getItems().isEmpty()) {
            comboBox.hide();
        } else {
            comboBox.show();
        }
    }

    private static String labelOf(MerchandiseOption merchandise, boolean useCode) {
        return String.valueOf(useCode ? merchandise.code() : merchandise.name());
    }
}
