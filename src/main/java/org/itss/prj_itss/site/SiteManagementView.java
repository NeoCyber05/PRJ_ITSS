package org.itss.prj_itss.site;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IInventoryDAO;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.entity.Site;

import java.util.List;

public class SiteManagementView {

    private final BorderPane view;
    private final ISiteDAO siteDAO;
    private final IInventoryDAO inventoryDAO;
    private final IMerchandiseDAO merchandiseDAO;

    public SiteManagementView(DAOFactory dao) {
        this.siteDAO = dao.getSiteDAO();
        this.inventoryDAO = dao.getInventoryDAO();
        this.merchandiseDAO = dao.getMerchandiseDAO();
        view = new BorderPane();
        view.getStyleClass().add("content-area");
        buildView();
    }

    private void buildView() {
        VBox header = new VBox(4); header.getStyleClass().add("page-header");
        Label t = new Label("Quản lý Site"); t.getStyleClass().add("page-title");
        Label d = new Label("Quản lý danh sách và thông tin các đối tác / nhà cung cấp nước ngoài");
        d.getStyleClass().add("page-description");
        header.getChildren().addAll(t, d);
        view.setTop(header);

        VBox content = new VBox(20); content.getStyleClass().add("page-content");
        content.getChildren().addAll(buildStatsRow(), buildActionBar(), buildSiteTable());
        ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent;");
        view.setCenter(sp);
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(16); row.setAlignment(Pos.CENTER_LEFT);
        int totalSites = siteDAO.countAll();
        int totalMerch = merchandiseDAO.countAll();
        row.getChildren().addAll(
            statCard(String.valueOf(totalSites), "Tổng số Site", "#2E6F40"),
            statCard(String.valueOf(totalSites), "Site đang hoạt động", "#68BA7F"),
            statCard(String.valueOf(totalMerch), "Mặt hàng kinh doanh", "#253D2C"));
        return row;
    }

    private VBox statCard(String value, String label, String color) {
        VBox card = new VBox(4); card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT); card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);
        Label vl = new Label(value); vl.getStyleClass().add("stat-value"); vl.setStyle("-fx-text-fill:"+color+";");
        Label tl = new Label(label); tl.getStyleClass().add("stat-label");
        card.getChildren().addAll(vl, tl); return card;
    }

    private HBox buildActionBar() {
        HBox bar = new HBox(12); bar.setAlignment(Pos.CENTER_LEFT);
        TextField sf = new TextField(); sf.setPromptText("🔍  Tìm kiếm site theo tên, mã, quốc gia...");
        sf.getStyleClass().add("search-field"); sf.setPrefWidth(400);
        HBox.setHgrow(sf, Priority.ALWAYS);
        Button ab = new Button("+ Thêm Site mới"); ab.getStyleClass().add("btn-primary");
        bar.getChildren().addAll(sf, ab); return bar;
    }

    @SuppressWarnings("unchecked")
    private VBox buildSiteTable() {
        VBox tc = new VBox(); tc.getStyleClass().add("card"); tc.setSpacing(0);
        List<Site> sites = siteDAO.findAll();
        HBox th = new HBox(); th.getStyleClass().add("card-header");
        Label tt = new Label("Danh sách Site đối tác"); tt.getStyleClass().add("card-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label cl = new Label(sites.size()+" site"); cl.getStyleClass().add("page-description");
        th.getChildren().addAll(tt, sp, cl);

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(46);
        TableColumn<String[],String> c0 = new TableColumn<>("MÃ SITE");
        c0.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue()[0])); c0.setPrefWidth(100);
        TableColumn<String[],String> c1 = new TableColumn<>("TÊN SITE");
        c1.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue()[1])); c1.setPrefWidth(200);
        TableColumn<String[],String> c2 = new TableColumn<>("MÔ TẢ");
        c2.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue()[2])); c2.setPrefWidth(180);
        TableColumn<String[],String> c3 = new TableColumn<>("VẬN CHUYỂN BIỂN");
        c3.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue()[3])); c3.setPrefWidth(130);
        TableColumn<String[],String> c4 = new TableColumn<>("VẬN CHUYỂN HÀNG KHÔNG");
        c4.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue()[4])); c4.setPrefWidth(160);
        TableColumn<String[],String> c5 = new TableColumn<>("SỐ MẶT HÀNG");
        c5.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue()[5])); c5.setPrefWidth(110);
        table.getColumns().addAll(c0,c1,c2,c3,c4,c5);

        ObservableList<String[]> data = FXCollections.observableArrayList();
        for (Site s : sites) {
            int ic = inventoryDAO.countMerchandiseAtSite(s.getId());
            data.add(new String[]{s.getSiteCode(), s.getName(), s.getDescription()!=null?s.getDescription():"",
                (s.getShipDeliveryDays()!=null?s.getShipDeliveryDays()+" ngày":"N/A"),
                (s.getAirDeliveryDays()!=null?s.getAirDeliveryDays()+" ngày":"N/A"), String.valueOf(ic)});
        }
        table.setItems(data);
        table.setPrefHeight(44 + (Math.max(1, data.size()) * table.getFixedCellSize()));
        tc.getChildren().addAll(th, table); return tc;
    }

    public Node getView() { return view; }
}
