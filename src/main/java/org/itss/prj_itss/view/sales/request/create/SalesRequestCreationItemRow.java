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
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
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
            Function<String, MerchandiseOption> merchandiseLookup,
            Consumer<SalesRequestCreationItemRow> deleteHandler
    ) {
        super(12);
        setAlignment(Pos.CENTER_LEFT);
        setStyle(ROW_STYLE);

        configureIndexLabel(index);
        configureCodeField(merchandiseLookup);
        configureQuantityField();
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
        indexLabel.setMinWidth(30);
        indexLabel.setAlignment(Pos.CENTER);
        indexLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        updateIndex(index);
    }

    private void configureCodeField(Function<String, MerchandiseOption> merchandiseLookup) {
        codeField.setPromptText("VD: MH-001");
        codeField.setMinWidth(150);
        codeField.setStyle(INPUT_STYLE);
        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            String code = newValue == null ? "" : newValue.trim();
            if (code.isEmpty()) {
                codeField.setStyle(INPUT_STYLE);
                unitField.setText("");
                return;
            }

            MerchandiseOption merchandise = merchandiseLookup.apply(code);
            if (merchandise == null) {
                codeField.setStyle(ERROR_STYLE);
                unitField.setText("");
                return;
            }

            codeField.setStyle(INPUT_STYLE);
            unitField.setText(merchandise.unit());
        });
    }

    private void configureQuantityField() {
        quantityField.setPromptText("0");
        quantityField.setMinWidth(100);
        quantityField.setStyle(INPUT_STYLE);
    }

    private void configureUnitField() {
        unitField.setPromptText("VD: Thùng");
        unitField.setMinWidth(120);
        unitField.setStyle(INPUT_STYLE);
        unitField.setEditable(false);
    }

    private void configureDesiredDatePicker() {
        desiredDatePicker.setPromptText("dd/mm/yyyy");
        desiredDatePicker.setMinWidth(180);
        desiredDatePicker.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4;");
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
            BigDecimal quantity = new BigDecimal(rawQuantity.trim());
            return quantity.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(quantity) : Optional.empty();
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
