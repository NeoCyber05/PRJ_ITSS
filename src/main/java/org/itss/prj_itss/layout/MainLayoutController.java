package org.itss.prj_itss.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.itss.prj_itss.home.HomeView;
import org.itss.prj_itss.order.OrderDetailView;
import org.itss.prj_itss.order.OrderManagementView;
import org.itss.prj_itss.request.ReceivedRequestsView;
import org.itss.prj_itss.request.RequestProcessingView;
import org.itss.prj_itss.site.SiteManagementView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


public class MainLayoutController {

    private final BorderPane root;
    private final StackPane contentArea;
    private final Label workspaceTitle;
    private final Label workspaceSubtitle;
    private final Label workspaceTag;
    private final Map<String, Button> navButtons;

    private final HomeView homeView;
    private final SiteManagementView siteManagementView;
    private final ReceivedRequestsView receivedRequestsView;
    private final OrderManagementView orderManagementView;

    public MainLayoutController() {
        root = new BorderPane();
        root.getStyleClass().add("app-shell");

        navButtons = new LinkedHashMap<>();
        workspaceTitle = new Label();
        workspaceTitle.getStyleClass().add("workspace-title");

        workspaceSubtitle = new Label();
        workspaceSubtitle.getStyleClass().add("workspace-subtitle");

        workspaceTag = new Label();
        workspaceTag.getStyleClass().add("workspace-pill");

        homeView = new HomeView(this);
        siteManagementView = new SiteManagementView();
        receivedRequestsView = new ReceivedRequestsView(this);
        orderManagementView = new OrderManagementView(this);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("workspace-content-shell");

        VBox workspaceShell = new VBox(18);
        workspaceShell.getStyleClass().add("workspace-shell");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        workspaceShell.getChildren().addAll(buildWorkspaceStrip(), contentArea);

        root.setLeft(buildSidebar());
        root.setCenter(workspaceShell);

        showView("home");
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(24);
        sidebar.getStyleClass().add("shell-sidebar");
        sidebar.setPadding(new Insets(28, 22, 22, 22));

        VBox brandBox = new VBox(6);
        Label brandKicker = new Label("ĐẶT HÀNG QUỐC TẾ");
        brandKicker.getStyleClass().add("shell-brand-kicker");

        Label brandTitle = new Label("ImportFlow");
        brandTitle.getStyleClass().add("shell-brand-title");

        Label brandSubtitle = new Label("Không gian làm việc của bộ phận đặt hàng");
        brandSubtitle.getStyleClass().add("shell-brand-subtitle");
        brandSubtitle.setWrapText(true);

        brandBox.getChildren().addAll(brandKicker, brandTitle, brandSubtitle);

        VBox navSection = new VBox(10);
        Label navLabel = new Label("ĐIỀU HƯỚNG");
        navLabel.getStyleClass().add("sidebar-section-label");

        navSection.getChildren().addAll(
            navLabel,
            createNavButton("Trang chủ", "home"),
            createNavButton("Quản lý site", "site-management"),
            createNavButton("Yêu cầu đã nhận", "received-requests"),
            createNavButton("Đơn hàng", "orders")
        );

        VBox pulseCard = new VBox(14);
        pulseCard.getStyleClass().add("sidebar-insight-card");

        Label pulseTitle = new Label("Tóm tắt nhanh");
        pulseTitle.getStyleClass().add("sidebar-insight-title");

        pulseCard.getChildren().addAll(
            pulseTitle,
            buildSidebarMetric("03", "yêu cầu chờ xử lý"),
            buildSidebarMetric("05", "đơn hàng đã tạo"),
            buildSidebarMetric("04", "site hoạt động")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox userCard = new HBox(12);
        userCard.getStyleClass().add("sidebar-user-card");
        userCard.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(18);
        avatar.setFill(Color.web("#5EEAD4"));

        Label avatarText = new Label("NA");
        avatarText.getStyleClass().add("sidebar-user-avatar-text");
        StackPane avatarPane = new StackPane(avatar, avatarText);

        VBox userMeta = new VBox(2);
        Label userName = new Label("Nguyễn Văn A");
        userName.getStyleClass().add("sidebar-user-name");
        Label userRole = new Label("Bộ phận đặt hàng quốc tế");
        userRole.getStyleClass().add("sidebar-user-role");
        userMeta.getChildren().addAll(userName, userRole);

        userCard.getChildren().addAll(avatarPane, userMeta);

        sidebar.getChildren().addAll(brandBox, navSection, pulseCard, spacer, userCard);
        return sidebar;
    }

    private HBox buildSidebarMetric(String value, String label) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("sidebar-metric-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label labelText = new Label(label);
        labelText.getStyleClass().add("sidebar-metric-label");

        row.getChildren().addAll(valueLabel, spacer, labelText);
        return row;
    }

    private Button createNavButton(String text, String viewId) {
        Button button = new Button(text);
        button.getStyleClass().add("shell-nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> showView(viewId));
        navButtons.put(viewId, button);
        return button;
    }

    private HBox buildWorkspaceStrip() {
        HBox strip = new HBox(16);
        strip.getStyleClass().add("workspace-strip");
        strip.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(5);

        Label eyebrow = new Label("KHÔNG GIAN LÀM VIỆC");
        eyebrow.getStyleClass().add("workspace-eyebrow");

        textGroup.getChildren().addAll(eyebrow, workspaceTitle, workspaceSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label datePill = new Label(
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy", new Locale("vi", "VN")))
        );
        datePill.getStyleClass().addAll("workspace-pill", "workspace-pill-soft");

        strip.getChildren().addAll(textGroup, spacer, workspaceTag, datePill);
        return strip;
    }

    public void showView(String viewId) {
        contentArea.getChildren().clear();
        updateWorkspaceMeta(viewId);
        setActiveNav(resolveNavTarget(viewId));

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
                if (viewId.startsWith("order-detail:")) {
                    String orderId = viewId.substring("order-detail:".length());
                    contentArea.getChildren().add(new OrderDetailView(this, orderId).getView());
                } else {
                    contentArea.getChildren().add(homeView.getView());
                }
        }
    }

    private void setActiveNav(String navTarget) {
        for (Button button : navButtons.values()) {
            button.getStyleClass().remove("shell-nav-button-active");
        }

        Button active = navButtons.get(navTarget);
        if (active != null) {
            active.getStyleClass().add("shell-nav-button-active");
        }
    }

    private String resolveNavTarget(String viewId) {
        if (viewId.startsWith("order-detail:")) {
            return "orders";
        }
        if ("request-processing".equals(viewId)) {
            return "received-requests";
        }
        return viewId;
    }

    private void updateWorkspaceMeta(String viewId) {
        String title = "Trang chủ";
        String subtitle = "Điểm vào nhanh cho các luồng chính của bộ phận đặt hàng quốc tế.";
        String tag = "Màn hình chính";

        if ("site-management".equals(viewId)) {
            title = "Quản lý site";
            subtitle = "Quản lý thông tin đối tác, thời gian vận chuyển và danh mục hàng.";
            tag = "Thông tin site";
        } else if ("received-requests".equals(viewId)) {
            title = "Yêu cầu đã nhận";
            subtitle = "Kiểm tra nhu cầu từ bộ phận bán hàng và mở xử lý khi cần.";
            tag = "Danh sách yêu cầu";
        } else if ("orders".equals(viewId)) {
            title = "Đơn hàng đã tạo";
            subtitle = "Theo dõi trạng thái xác nhận, giao hàng và hoàn thành đơn hàng.";
            tag = "Theo dõi đơn hàng";
        } else if ("request-processing".equals(viewId)) {
            title = "Xử lý yêu cầu";
            subtitle = "Phân bổ theo hạn giao, tồn kho và số lượng site phù hợp.";
            tag = "Đang xử lý";
        } else if (viewId.startsWith("order-detail:")) {
            String orderId = viewId.substring("order-detail:".length());
            title = "Chi tiết đơn hàng";
            subtitle = "Xem tiến trình giao hàng, danh sách mặt hàng và thông tin site của " + orderId + ".";
            tag = orderId;
        }

        workspaceTitle.setText(title);
        workspaceSubtitle.setText(subtitle);
        workspaceTag.setText(tag);
    }

    public BorderPane getRoot() {
        return root;
    }
}
