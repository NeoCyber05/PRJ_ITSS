package org.itss.prj_itss;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;


public class MainLayoutController {

    private final BorderPane root;
    private final StackPane contentArea;
    private Button activeNavBtn;

    // View controllers
    private final HomeView homeView;
    private final SiteManagementView siteManagementView;
    private final ReceivedRequestsView receivedRequestsView;
    private final OrderManagementView orderManagementView;

    public MainLayoutController() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F9F6;");

        // Initialize views
        homeView             = new HomeView(this);
        siteManagementView   = new SiteManagementView();
        receivedRequestsView = new ReceivedRequestsView(this);
        orderManagementView  = new OrderManagementView(this);

        // Build header
        VBox headerSection = buildHeader();

        // Content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #F5F9F6;");

        root.setTop(headerSection);
        root.setCenter(contentArea);

        // Default view = Home
        showView("home");
    }

    // =========================================================================
    // HEADER NAVIGATION
    // =========================================================================
    private VBox buildHeader() {
        VBox headerWrapper = new VBox(0);

        // --- Row 1: Top brand bar ---
        HBox brandBar = new HBox(12);
        brandBar.setAlignment(Pos.CENTER_LEFT);
        brandBar.setPadding(new Insets(10, 36, 10, 36));
        brandBar.setStyle(
            "-fx-background-color: #253D2C;"
        );

        Label logo = new Label("🌐 Import Order System");
        logo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label dept = new Label("Bộ phận đặt hàng quốc tế");
        dept.setStyle("-fx-font-size: 12px; -fx-text-fill: #A8D8B4;");

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        // User info
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(14);
        avatar.setFill(Color.web("#68BA7F"));

        Label avatarText = new Label("A");
        avatarText.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        StackPane avatarPane = new StackPane(avatar, avatarText);

        Label userName = new Label("Nguyễn Văn A");
        userName.setStyle("-fx-font-size: 13px; -fx-text-fill: #CFFFDC;");

        userBox.getChildren().addAll(avatarPane, userName);

        brandBar.getChildren().addAll(logo, dept, sp1, userBox);

        // --- Row 2: Navigation tabs ---
        HBox navBar = new HBox(0);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(0, 28, 0, 28));
        navBar.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent #D8E8DD transparent;" +
            "-fx-border-width: 0 0 2 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 2);"
        );

        Button btnHome     = createNavButton("🏠  Trang chủ",        "home");
        Button btnSite     = createNavButton("📋  Quản lý Site",     "site-management");
        Button btnRequests = createNavButton("📥  Yêu cầu đã nhận", "received-requests");
        Button btnOrders   = createNavButton("📦  Đơn hàng",        "orders");

        navBar.getChildren().addAll(btnHome, btnSite, btnRequests, btnOrders);

        // Mark Home as active
        setActiveNav(btnHome);

        headerWrapper.getChildren().addAll(brandBar, navBar);
        return headerWrapper;
    }

    private Button createNavButton(String text, String viewId) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #6B7C72;" +
            "-fx-font-size: 13.5px;" +
            "-fx-padding: 14 22;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-border-color: transparent transparent transparent transparent;" +
            "-fx-border-width: 0 0 3 0;"
        );
        btn.setOnAction(e -> {
            setActiveNav(btn);
            showView(viewId);
        });

        // Hover effect
        String normalStyle =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #6B7C72;" +
            "-fx-font-size: 13.5px;" +
            "-fx-padding: 14 22;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent transparent transparent transparent;" +
            "-fx-border-width: 0 0 3 0;";
        String hoverStyle =
            "-fx-background-color: #F5FDF7;" +
            "-fx-text-fill: #2E6F40;" +
            "-fx-font-size: 13.5px;" +
            "-fx-padding: 14 22;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent transparent #68BA7F transparent;" +
            "-fx-border-width: 0 0 3 0;";

        btn.setOnMouseEntered(e -> {
            if (btn != activeNavBtn) btn.setStyle(hoverStyle);
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeNavBtn) btn.setStyle(normalStyle);
        });

        return btn;
    }

    private void setActiveNav(Button btn) {
        // Reset old active
        if (activeNavBtn != null) {
            activeNavBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #6B7C72;" +
                "-fx-font-size: 13.5px;" +
                "-fx-padding: 14 22;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: transparent transparent transparent transparent;" +
                "-fx-border-width: 0 0 3 0;"
            );
        }
        // Set new active
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #2E6F40;" +
            "-fx-font-size: 13.5px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 14 22;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent transparent #2E6F40 transparent;" +
            "-fx-border-width: 0 0 3 0;"
        );
        activeNavBtn = btn;
    }

    // =========================================================================
    // VIEW SWITCHING
    // =========================================================================
    public void showView(String viewId) {
        contentArea.getChildren().clear();
        switch (viewId) {
            case "home":
                contentArea.getChildren().add(homeView.getView());
                break;
            case "site-management":
                contentArea.getChildren().add(siteManagementView.getView());
                break;
            case "received-requests":
                contentArea.getChildren().add(receivedRequestsView.getView());
                break;
            case "orders":
                contentArea.getChildren().add(orderManagementView.getView());
                break;
            case "request-processing":
                contentArea.getChildren().add(new RequestProcessingView(this).getView());
                break;
            default:
                // Handle order-detail-XXX
                if (viewId.startsWith("order-detail:")) {
                    String orderId = viewId.substring("order-detail:".length());
                    contentArea.getChildren().add(new OrderDetailView(this, orderId).getView());
                } else {
                    contentArea.getChildren().add(homeView.getView());
                }
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}
