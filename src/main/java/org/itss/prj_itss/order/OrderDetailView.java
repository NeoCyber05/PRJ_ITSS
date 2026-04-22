package org.itss.prj_itss.order;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.layout.Navigator;

public class OrderDetailView {

    private final StackPane view;

    public OrderDetailView(Navigator navigator, DAOFactory daoFactory, String orderId) {
        view = new StackPane();
        view.setStyle("-fx-background-color: #F3F7FB;");

        Node background = new OrderManagementView(navigator, daoFactory).getView();
        background.setEffect(new GaussianBlur(14));
        background.setOpacity(0.96);

        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(15,23,42,0.34);");
        backdrop.setOnMouseClicked(event -> navigator.showView("orders"));

        Node drawer = new OrderDetailPanel(orderId, () -> navigator.showView("orders"), daoFactory).getView();

        HBox drawerLayer = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        drawerLayer.getChildren().addAll(spacer, drawer);
        drawerLayer.setAlignment(Pos.CENTER_RIGHT);

        view.getChildren().addAll(background, backdrop, drawerLayer);
    }

    public Node getView() {
        return view;
    }
}
