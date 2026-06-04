package org.itss.prj_itss.view.warehouse;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.beans.property.SimpleStringProperty;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderDetail;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderItemRow;

public final class IncomingOrderDetailDialog {

    private IncomingOrderDetailDialog() {
    }

    public static void show(Window owner, IncomingOrderDetail detail) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Chi tiết đơn hàng " + detail.summary().orderCode());

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(640);

        VBox summaryBox = new VBox(6);
        summaryBox.getChildren().addAll(
            new Label("Mã đơn hàng: " + detail.summary().orderCode()),
            new Label("Mã yêu cầu: " + detail.summary().requestCode()),
            new Label("Mã site: " + detail.summary().siteCode()),
            new Label("Tên site: " + detail.summary().siteName()),
            new Label("Ngày tạo: " + detail.summary().createdAt()),
            new Label("Trạng thái: " + detail.summary().statusText())
        );

        TableView<IncomingOrderItemRow> itemTable = new TableView<>();
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemTable.setPrefHeight(200);

        TableColumn<IncomingOrderItemRow, String> codeCol = new TableColumn<>("Mã hàng");
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseCode()));
        TableColumn<IncomingOrderItemRow, String> nameCol = new TableColumn<>("Tên mặt hàng");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseName()));
        TableColumn<IncomingOrderItemRow, String> unitCol = new TableColumn<>("Đơn vị");
        unitCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        TableColumn<IncomingOrderItemRow, String> qtyCol = new TableColumn<>("Số lượng");
        qtyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderedQuantity()));
        TableColumn<IncomingOrderItemRow, String> methodCol = new TableColumn<>("Phương thức");
        methodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().deliveryMethod()));

        itemTable.getColumns().addAll(codeCol, nameCol, unitCol, qtyCol, methodCol);
        itemTable.getItems().addAll(detail.items());

        root.getChildren().addAll(summaryBox, new Label("Danh sách mặt hàng:"), itemTable);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
