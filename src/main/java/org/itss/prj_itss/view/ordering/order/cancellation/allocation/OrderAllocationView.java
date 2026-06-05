package org.itss.prj_itss.view.ordering.order.cancellation.allocation;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.view.ordering.order.cancellation.OrderCancellationLayoutView;
import org.itss.prj_itss.view.ordering.order.cancellation.state.CancelledOrderProcessingViewModel;
import org.itss.prj_itss.view.ordering.request.process.state.AllocationChangeCommand;
import org.itss.prj_itss.view.ordering.request.process.state.AllocationChangeResultView;

public final class OrderAllocationView {

    private OrderCancellationLayoutView layoutView;

    @FXML
    private VBox container;

    public void init(OrderCancellationLayoutView layoutView) {
        this.layoutView = layoutView;
        render();
    }

    private void render() {
        if (layoutView == null || layoutView.getCurrentViewModel() == null) {
            return;
        }
        container.getChildren().setAll(
            layoutView.getCurrentViewModel().allocationItems().stream()
                .map(this::buildItemGroupCard)
                .toList()
        );
    }

    private VBox buildItemGroupCard(CancelledOrderProcessingViewModel.AllocationItemViewModel group) {
        VBox card = new VBox(0);
        card.getStyleClass().add("cancelled-order-section-card");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("cancelled-order-section-header");
        header.setPadding(new Insets(15, 18, 15, 18));

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(group.name());
        nameLabel.getStyleClass().add("cancelled-order-section-title");
        
        String desiredDateText = layoutView.getCurrentViewModel().desiredDeliveryDates().get(group.merchandiseId());
        Label desiredDateLabel = new Label("Ngày cần: " + (desiredDateText == null ? "" : desiredDateText));
        desiredDateLabel.getStyleClass().add("cancelled-order-deadline-badge");
        Label metaLabel = new Label(group.siteRows().size() + " site  •  Tổng tồn: " + group.totalStock());
        metaLabel.getStyleClass().add("cancelled-order-muted-text");
        titleRow.getChildren().addAll(nameLabel, desiredDateLabel);
        titleBox.getChildren().addAll(titleRow, metaLabel);

        Label statusBadge = new Label(group.allocationFractionText());
        boolean fulfilled = group.allocated() >= group.required();
        statusBadge.getStyleClass().addAll("cancelled-order-pill", fulfilled ? "cancelled-order-pill-success" : "cancelled-order-pill-warning");

        Button toggleButton = new Button(group.expanded() ? "⌃" : "⌄");
        toggleButton.getStyleClass().add("cancelled-order-icon-button");
        toggleButton.setOnAction(event -> {
            int index = layoutView.getCurrentViewModel().allocationItems().indexOf(group);
            layoutView.getController().toggleExpandedItem(index);
            layoutView.showAllocationScreen();
        });

        header.getChildren().addAll(titleBox, statusBadge, toggleButton);
        card.getChildren().add(header);
        if (group.expanded()) {
            card.getChildren().add(buildAllocationTable(group));
        }
        return card;
    }

    private VBox buildAllocationTable(CancelledOrderProcessingViewModel.AllocationItemViewModel group) {
        VBox table = new VBox(0);
        table.getStyleClass().add("cancelled-order-table");
        table.getChildren().add(buildAllocationStatusHeaderRow());
        for (CancelledOrderProcessingViewModel.AllocationSiteRowViewModel site : group.siteRows()) {
            table.getChildren().add(buildAllocationStatusRow(group, site));
        }
        return table;
    }

    private HBox buildAllocationStatusHeaderRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-header");
        row.getChildren().addAll(
            headerCell("MÃ SITE", 92),
            headerCell("TÊN SITE", 150),
            headerCell("TỒN KHO", 90),
            headerCell("VẬN CHUYỂN", 150),
            headerCell("TRẠNG THÁI", 150),
            headerCell("SL ĐẶT", 100)
        );
        return row;
    }

    private HBox buildAllocationStatusRow(
        CancelledOrderProcessingViewModel.AllocationItemViewModel group,
        CancelledOrderProcessingViewModel.AllocationSiteRowViewModel site
    ) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-row");

        ComboBox<String> transportBox = new ComboBox<>();
        transportBox.getItems().setAll(site.transportLabels());
        transportBox.setValue(site.selectedTransportLabel());
        transportBox.setPrefWidth(128);
        transportBox.getStyleClass().add("cancelled-order-combo");
        transportBox.setDisable(site.transportDisabled());
        transportBox.setOnAction(event -> {
            if (transportBox.getValue() != null && !transportBox.getValue().equals(site.selectedTransportLabel())) {
                handleAllocationChanged(group, site, String.valueOf(site.quantity()), transportBox.getValue());
            }
        });

        TextField quantityField = new TextField(site.quantity() == 0 ? "" : String.valueOf(site.quantity()));
        quantityField.setPromptText("0");
        quantityField.setPrefWidth(78);
        quantityField.getStyleClass().add("cancelled-order-quantity-field");
        
        quantityField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Focus lost
                String currentText = quantityField.getText().trim();
                handleAllocationChanged(group, site, currentText, transportBox.getValue());
            }
        });
        quantityField.setOnAction(event -> {
            String currentText = quantityField.getText().trim();
            handleAllocationChanged(group, site, currentText, transportBox.getValue());
        });

        row.getChildren().addAll(
            valueCell(site.siteDetail().split(" - ")[0], 92, "cancelled-order-code-badge"),
            valueCell(site.siteName(), 150, null),
            valueCell(String.valueOf(site.stock()), 90, "cancelled-order-stock-badge"),
            wrappedCell(transportBox, 150),
            valueCell(site.deliveryStatusText(), 150, site.deliveryStatusClass()),
            wrappedCell(quantityField, 100)
        );
        return row;
    }

    private void handleAllocationChanged(
        CancelledOrderProcessingViewModel.AllocationItemViewModel group,
        CancelledOrderProcessingViewModel.AllocationSiteRowViewModel site,
        String quantityText,
        String transportLabel
    ) {
        AllocationChangeCommand command = new AllocationChangeCommand(
            group.merchandiseId(),
            site.siteId(),
            quantityText,
            transportLabel
        );
        AllocationChangeResultView result = layoutView.getController().handleAllocationInputChanged(command);
        if (!result.applied()) {
            String message = switch (result.errorType()) {
                case "INVALID_INTEGER" -> "Vui lòng nhập số nguyên hợp lệ.";
                case "NEGATIVE_QUANTITY" -> "Số lượng không được âm.";
                case "EXCEEDS_STOCK" -> "Số lượng vượt quá tồn kho khả dụng (" + result.stock() + ").";
                default -> "Lỗi nhập liệu.";
            };
            layoutView.showFeedback(message, OrderCancellationLayoutView.FeedbackKind.ERROR);
        } else {
            layoutView.showFeedback("Đã cập nhật phân bổ thành công.", OrderCancellationLayoutView.FeedbackKind.SUCCESS);
        }
        layoutView.showAllocationScreen();
    }

    private Label headerCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.getStyleClass().add("cancelled-order-table-header-cell");
        return label;
    }

    private StackPane valueCell(String text, double width, String valueStyleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("cancelled-order-table-cell-text");
        if (valueStyleClass != null) {
            label.getStyleClass().add(valueStyleClass);
        }
        return wrappedCell(label, width);
    }

    private StackPane wrappedCell(Node child, double width) {
        StackPane wrapper = new StackPane(child);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setMinWidth(width);
        wrapper.setPrefWidth(width);
        wrapper.setPadding(new Insets(0, 12, 0, 12));
        return wrapper;
    }
}
