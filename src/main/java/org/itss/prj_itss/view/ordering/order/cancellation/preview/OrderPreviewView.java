package org.itss.prj_itss.view.ordering.order.cancellation.preview;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.view.ordering.order.cancellation.OrderCancellationLayoutView;
import org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView;

public final class OrderPreviewView {

    private OrderCancellationLayoutView layoutView;

    @FXML
    private VBox container;

    public void init(OrderCancellationLayoutView layoutView) {
        this.layoutView = layoutView;
        render();
    }

    private void render() {
        if (layoutView == null || layoutView.getCurrentPreviewOrders() == null) {
            return;
        }
        container.getChildren().setAll(
            layoutView.getCurrentPreviewOrders().stream()
                .map(this::buildPreviewOrderCard)
                .toList()
        );
    }

    private VBox buildPreviewOrderCard(ProcessingPreviewOrderView order) {
        return buildOrderCard(order, false);
    }

    private VBox buildOrderCard(ProcessingPreviewOrderView order, boolean includeStatus) {
        VBox card = new VBox(0);
        card.getStyleClass().add("cancelled-order-section-card");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 18, 15, 18));
        header.getStyleClass().add("cancelled-order-section-header");

        Label title = new Label("Đơn hàng " + order.siteName());
        title.getStyleClass().add("cancelled-order-section-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label code = new Label(order.siteCode());
        code.getStyleClass().add("cancelled-order-muted-text");
        header.getChildren().addAll(title, code);
        if (includeStatus) {
            Label status = new Label("● Chờ xác nhận");
            status.getStyleClass().addAll("cancelled-order-pill", "cancelled-order-pill-warning");
            header.getChildren().add(status);
        }

        VBox table = new VBox(0);
        table.getStyleClass().add("cancelled-order-table");
        table.getChildren().add(buildPreviewHeaderRow(includeStatus));
        for (ProcessingPreviewOrderView.ProcessingPreviewLineView line : order.lines()) {
            table.getChildren().add(buildPreviewRow(line, includeStatus));
        }

        card.getChildren().addAll(header, table);
        return card;
    }

    private HBox buildPreviewHeaderRow(boolean includeStatus) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-header");
        row.getChildren().addAll(
            headerCell("MẶT HÀNG", 240),
            headerCell("SỐ LƯỢNG", 120),
            headerCell("VẬN CHUYỂN", 160)
        );
        if (includeStatus) {
            row.getChildren().add(headerCell("TRẠNG THÁI", 160));
        }
        return row;
    }

    private HBox buildPreviewRow(ProcessingPreviewOrderView.ProcessingPreviewLineView line, boolean includeStatus) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-row");
        row.getChildren().addAll(
            valueCell(line.merchandiseName(), 240, null),
            valueCell(String.valueOf(line.quantity()), 120, "cancelled-order-stock-badge"),
            valueCell(line.transport(), 160, null)
        );
        if (includeStatus) {
            row.getChildren().add(valueCell("● Chờ xác nhận", 160, "cancelled-order-date-sea"));
        }
        return row;
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
