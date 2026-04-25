package org.itss.prj_itss.ordering.order;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.itss.prj_itss.App;
import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;

public class OrderDetailView {

    private final StackPane view;

    public OrderDetailView(INavigator navigator, ApplicationContext context, String orderId) {
        view = new StackPane();
        view.setStyle("-fx-background-color: #F3F7FB;");

        Node background = loadOrdersBackground(navigator, context);
        background.setEffect(new GaussianBlur(14));
        background.setOpacity(0.96);

        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(15,23,42,0.34);");
        backdrop.setOnMouseClicked(event -> navigator.showView("orders"));

        Node drawer = new OrderDetailPanel(orderId, () -> navigator.showView("orders"), context).getView();

        HBox drawerLayer = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        drawerLayer.getChildren().addAll(spacer, drawer);
        drawerLayer.setAlignment(Pos.CENTER_RIGHT);

        view.getChildren().addAll(background, backdrop, drawerLayer);
    }

    private Node loadOrdersBackground(INavigator navigator, ApplicationContext context) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/ordering/order/order-management-view.fxml"));
            Node background = loader.load();
            Object controller = loader.getController();
            if (controller instanceof IViewController viewController) {
                viewController.init(navigator, context);
            }
            return background;
        } catch (Exception exception) {
            Label errorLabel = new Label("Khong the tai danh sach don hang.");
            StackPane fallback = new StackPane(errorLabel);
            fallback.getStyleClass().add("content-area");
            return fallback;
        }
    }

    public Node getView() {
        return view;
    }
}
