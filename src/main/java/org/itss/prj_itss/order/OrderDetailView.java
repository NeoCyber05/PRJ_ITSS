package org.itss.prj_itss.order;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import org.itss.prj_itss.layout.MainLayoutController;

public class OrderDetailView {

    private final BorderPane view;

    public OrderDetailView(MainLayoutController mainController, String orderId) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: #F5F9F6;");

        Node detailContent = new OrderDetailPanel(orderId, () -> mainController.showView("orders")).getView();

        StackPane wrapper = new StackPane(detailContent);
        wrapper.setPadding(new Insets(24, 36, 36, 36));
        wrapper.setStyle("-fx-background-color: #F5F9F6;");

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: #F5F9F6; -fx-background: #F5F9F6;");

        view.setCenter(scrollPane);
    }

    public Node getView() {
        return view;
    }
}
