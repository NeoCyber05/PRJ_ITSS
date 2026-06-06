package org.itss.prj_itss.view.sales.request.create;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.itss.prj_itss.controller.shared.MerchandiseOptionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.util.function.Consumer;
import java.util.function.Function;

final class SalesRequestCreationItemRow extends HBox {

    private static final String ROW_STYLE = "-fx-background-color: #F8FAFC; -fx-background-radius: 6; "
        + "-fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-padding: 8;";
    private static final String INPUT_STYLE = "-fx-background-color: white; -fx-border-color: #CBD5E1; "
        + "-fx-border-radius: 4; -fx-padding: 6 10;";
    private static final String ERROR_STYLE = "-fx-background-color: #FEF2F2; -fx-border-color: #EF4444; "
        + "-fx-border-radius: 4; -fx-padding: 6 10;";

    private final Label indexLabel = new Label();
    private final TextField codeField = new TextField();
    private final TextField quantityField = new TextField();
    private final TextField unitField = new TextField();
    private final DatePicker desiredDatePicker = new DatePicker();
    private final Button deleteButton = new Button();

    SalesRequestCreationItemRow(
            int index,
            Function<String, MerchandiseOptionDTO> merchandiseLookup,
            Function<String, List<String>> codeSuggester,
            Function<String, Integer> stockLookup,
            Consumer<SalesRequestCreationItemRow> deleteHandler
    ) {
        super(12);
        setAlignment(Pos.CENTER_LEFT);
        setStyle(ROW_STYLE);

        configureIndexLabel(index);
        configureCodeField(merchandiseLookup, codeSuggester, stockLookup);
        configureQuantityField(merchandiseLookup, stockLookup);
        configureUnitField();
        configureDesiredDatePicker();
        configureDeleteButton(deleteHandler);

        getChildren().addAll(indexLabel, codeField, quantityField, unitField, desiredDatePicker, deleteButton);
    }

    void updateIndex(int index) {
        indexLabel.setText(String.valueOf(index));
    }

    Optional<SalesRequestCreationItemCandidate> inputCandidate() {
        String code = codeField.getText();
        String rawQuantity = quantityField.getText();
        LocalDate desiredDate = desiredDatePicker.getValue();
        BigDecimal quantity = parseQuantity(rawQuantity).orElse(null);
        return Optional.of(new SalesRequestCreationItemCandidate(
            code == null ? "" : code.trim(),
            quantity,
            desiredDate
        ));
    }

    private void configureIndexLabel(int index) {
        indexLabel.setPrefWidth(30);
        indexLabel.setMaxWidth(30);
        indexLabel.setAlignment(Pos.CENTER);
        indexLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        updateIndex(index);
    }

    private void configureCodeField(Function<String, MerchandiseOptionDTO> merchandiseLookup, Function<String, List<String>> codeSuggester, Function<String, Integer> stockLookup) {
        codeField.setPromptText("VD: MH-001");
        codeField.setPrefWidth(150);
        codeField.setMaxWidth(150);
        codeField.setStyle(INPUT_STYLE);
        
        ContextMenu suggestionPopup = new ContextMenu();
        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            suggestionPopup.getItems().clear();
            String code = newValue == null ? "" : newValue.trim();
            if (!code.isEmpty()) {
                codeSuggester.apply(code).forEach(c -> {
                        MenuItem item = new MenuItem(c);
                        item.setOnAction(e -> {
                            codeField.setText(c);
                            codeField.positionCaret(c.length());
                        });
                        suggestionPopup.getItems().add(item);
                    });
                if (!suggestionPopup.getItems().isEmpty() && codeField.isFocused()) {
                    suggestionPopup.show(codeField, Side.BOTTOM, 0, 0);
                } else {
                    suggestionPopup.hide();
                }
            } else {
                suggestionPopup.hide();
            }

            if (code.isEmpty()) {
                codeField.setStyle(INPUT_STYLE);
                unitField.setText("");
                return;
            }

            MerchandiseOptionDTO merchandise = merchandiseLookup.apply(code);
            if (merchandise == null) {
                codeField.setStyle(ERROR_STYLE);
                unitField.setText("");
                return;
            }

            codeField.setStyle(INPUT_STYLE);
            unitField.setText(merchandise.unit());
            validateQuantity(merchandiseLookup, stockLookup);
        });
    }

    private void configureQuantityField(Function<String, MerchandiseOptionDTO> merchandiseLookup, Function<String, Integer> stockLookup) {
        quantityField.setPromptText("0");
        quantityField.setPrefWidth(100);
        quantityField.setMaxWidth(100);
        quantityField.setStyle(INPUT_STYLE);
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateQuantity(merchandiseLookup, stockLookup);
        });
    }

    private void validateQuantity(Function<String, MerchandiseOptionDTO> merchandiseLookup, Function<String, Integer> stockLookup) {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            quantityField.setStyle(INPUT_STYLE);
            return;
        }
        MerchandiseOptionDTO merchandise = merchandiseLookup.apply(code);
        if (merchandise == null) {
            quantityField.setStyle(INPUT_STYLE);
            return;
        }
        Optional<BigDecimal> qtyOpt = parseQuantity(quantityField.getText());
        if (qtyOpt.isPresent()) {
            BigDecimal qty = qtyOpt.get();
            Integer stock = stockLookup.apply(code);
            if (stock != null && qty.compareTo(new BigDecimal(stock)) > 0) {
                quantityField.setStyle(ERROR_STYLE);
            } else {
                quantityField.setStyle(INPUT_STYLE);
            }
        } else {
            String text = quantityField.getText();
            if (text != null && !text.isBlank()) {
                quantityField.setStyle(ERROR_STYLE);
            } else {
                quantityField.setStyle(INPUT_STYLE);
            }
        }
    }

    private void configureUnitField() {
        unitField.setPromptText("VD: Thùng");
        unitField.setPrefWidth(120);
        unitField.setMaxWidth(120);
        unitField.setStyle(INPUT_STYLE);
        unitField.setEditable(false);
    }

    private void configureDesiredDatePicker() {
        desiredDatePicker.setPromptText("dd/MM/yyyy");
        desiredDatePicker.setPrefWidth(180);
        desiredDatePicker.setMaxWidth(180);
        desiredDatePicker.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4;");
        
        javafx.util.StringConverter<LocalDate> converter = new javafx.util.StringConverter<LocalDate>() {
            java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
        desiredDatePicker.setConverter(converter);

        // Force commit text on focus loss
        desiredDatePicker.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    desiredDatePicker.setValue(desiredDatePicker.getConverter().fromString(desiredDatePicker.getEditor().getText()));
                } catch (Exception e) {
                    desiredDatePicker.setValue(null);
                }
            }
        });

        desiredDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isBefore(LocalDate.now())) {
                desiredDatePicker.setStyle(ERROR_STYLE);
            } else {
                desiredDatePicker.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");
            }
        });

        desiredDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    return;
                }
                LocalDate today = LocalDate.now();
                if (date.isBefore(today)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-border-color: transparent;");
                } else if (desiredDatePicker.getValue() != null && date.equals(desiredDatePicker.getValue())) {
                    setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #1e3a8a; -fx-font-weight: bold; -fx-border-color: transparent;");
                } else if (date.equals(today)) {
                    setStyle("-fx-border-color: transparent;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configureDeleteButton(Consumer<SalesRequestCreationItemRow> deleteHandler) {
        deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19V4M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z");
        trashIcon.setFill(Color.web("#EF4444"));
        trashIcon.setScaleX(0.8);
        trashIcon.setScaleY(0.8);
        deleteButton.setGraphic(trashIcon);
        deleteButton.setOnAction(event -> deleteHandler.accept(this));
    }

    private Optional<BigDecimal> parseQuantity(String rawQuantity) {
        if (rawQuantity == null || rawQuantity.isBlank()) {
            return Optional.empty();
        }
        try {
            String trimmed = rawQuantity.trim();
            if (!trimmed.matches("-?\\d+")) {
                return Optional.empty();
            }
            BigDecimal quantity = new BigDecimal(trimmed);
            return quantity.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(quantity) : Optional.empty();
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
