package org.itss.prj_itss.site;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class SiteManagementView {

    private final BorderPane view;

    public SiteManagementView() {
        view = new BorderPane();
        view.getStyleClass().add("content-area");
        buildView();
    }

    private void buildView() {
        // === Page Header ===
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");

        Label pageTitle = new Label("Quản lý Site");
        pageTitle.getStyleClass().add("page-title");

        Label pageDesc = new Label("Quản lý danh sách và thông tin các đối tác / nhà cung cấp nước ngoài");
        pageDesc.getStyleClass().add("page-description");

        header.getChildren().addAll(pageTitle, pageDesc);
        view.setTop(header);

        // === Content ===
        VBox content = new VBox(20);
        content.getStyleClass().add("page-content");

        // Stats row
        HBox statsRow = buildStatsRow();

        // Search + Add button bar
        HBox actionBar = buildActionBar();

        // Site table
        VBox tableCard = buildSiteTable();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        content.getChildren().addAll(statsRow, actionBar, tableCard);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        view.setCenter(scrollPane);
    }

    private HBox buildStatsRow() {
        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        statsRow.getChildren().addAll(
                createStatCard("4", "Tổng số Site", "#2E6F40"),
                createStatCard("3", "Site đang hoạt động", "#68BA7F"),
                createStatCard("12", "Mặt hàng kinh doanh", "#253D2C"),
                createStatCard("1", "Chờ duyệt", "#E65100")
        );

        return statsRow;
    }

    private VBox createStatCard(String value, String label, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }

    private HBox buildActionBar() {
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm kiếm site theo tên, mã, quốc gia...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(400);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button addBtn = new Button("+ Thêm Site mới");
        addBtn.getStyleClass().addAll("btn-primary");

        actionBar.getChildren().addAll(searchField, addBtn);
        return actionBar;
    }

    private VBox buildSiteTable() {
        VBox tableCard = new VBox();
        tableCard.getStyleClass().add("card");
        tableCard.setSpacing(0);

        // Table header
        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("card-header");
        Label tableTitle = new Label("Danh sách Site đối tác");
        tableTitle.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLabel = new Label("4 site");
        countLabel.getStyleClass().add("page-description");
        tableHeader.getChildren().addAll(tableTitle, spacer, countLabel);

        // Build table
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        TableColumn<String[], String> colCode = new TableColumn<>("MÃ SITE");
        colCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        colCode.setPrefWidth(100);

        TableColumn<String[], String> colName = new TableColumn<>("TÊN SITE");
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        colName.setPrefWidth(200);

        TableColumn<String[], String> colCountry = new TableColumn<>("QUỐC GIA");
        colCountry.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        colCountry.setPrefWidth(120);

        TableColumn<String[], String> colShipDays = new TableColumn<>("VẬN CHUYỂN BIỂN");
        colShipDays.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        colShipDays.setPrefWidth(130);

        TableColumn<String[], String> colAirDays = new TableColumn<>("VẬN CHUYỂN HÀNG KHÔNG");
        colAirDays.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));
        colAirDays.setPrefWidth(160);

        TableColumn<String[], String> colItems = new TableColumn<>("SỐ MẶT HÀNG");
        colItems.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        colItems.setPrefWidth(110);

        TableColumn<String[], String> colStatus = new TableColumn<>("TRẠNG THÁI");
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[6]));
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("Hoạt động".equals(item)) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-pending");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        table.getColumns().addAll(colCode, colName, colCountry, colShipDays, colAirDays, colItems, colStatus);

        // Sample data
        ObservableList<String[]> data = FXCollections.observableArrayList(
                new String[]{"SITE001", "Tokyo Electronics Hub", "Nhật Bản", "14 ngày", "3 ngày", "5", "Hoạt động"},
                new String[]{"SITE002", "Seoul Tech Supply", "Hàn Quốc", "12 ngày", "3 ngày", "4", "Hoạt động"},
                new String[]{"SITE003", "Shenzhen Import Co.", "Trung Quốc", "10 ngày", "2 ngày", "6", "Hoạt động"},
                new String[]{"SITE004", "Singapore Trade Center", "Singapore", "8 ngày", "2 ngày", "3", "Chờ duyệt"}
        );
        table.setItems(data);

        tableCard.getChildren().addAll(tableHeader, table);
        return tableCard;
    }

    public Node getView() {
        return view;
    }
}
