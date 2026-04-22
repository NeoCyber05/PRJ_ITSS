package org.itss.prj_itss.order;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.itss.prj_itss.common.AppStyles;
import org.itss.prj_itss.common.StatusBadgeFactory;
import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IOrderDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.Navigator;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderManagementView {

    private final BorderPane view;
    private final Navigator navigator;
    private final IOrderDAO orderDAO;
    private final ISiteDAO siteDAO;
    private final IMerchandiseDAO merchandiseDAO;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OrderManagementView(Navigator navigator, DAOFactory dao) {
        this.navigator = navigator;
        this.orderDAO = dao.getOrderDAO();
        this.siteDAO = dao.getSiteDAO();
        this.merchandiseDAO = dao.getMerchandiseDAO();
        view = new BorderPane();
        view.getStyleClass().add("content-area");
        buildView();
    }

    private void buildView() {
        VBox header = new VBox(6); header.setStyle(AppStyles.pageHeaderStyle());
        Label t = new Label("Quản lý đơn hàng"); t.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        header.getChildren().add(t);
        view.setTop(header);
        VBox content = new VBox(0); content.setStyle("-fx-background-color:white;");
        content.getChildren().addAll(buildFilterBar(), buildTable(), buildPagination());
        ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color:white;-fx-background:white;"); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        view.setCenter(sp);
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(12); bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(20,32,20,32)); bar.setStyle(AppStyles.filterBarStyle());
        HBox sb = new HBox(8); sb.setAlignment(Pos.CENTER_LEFT); sb.setStyle(AppStyles.searchBoxStyle());
        sb.setPrefWidth(280); sb.setMinHeight(40);
        Label ic = new Label("🔍"); ic.setStyle("-fx-font-size:13px;");
        TextField tf = new TextField(); tf.setPromptText("Tìm mã đơn hàng...");
        tf.setStyle(AppStyles.searchFieldStyle()); HBox.setHgrow(tf,Priority.ALWAYS);
        sb.getChildren().addAll(ic,tf);
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("Mọi trạng thái","Chờ xác nhận","Đang giao","Đã hoàn thành","Đã hủy");
        cb.setValue("Mọi trạng thái"); cb.setStyle(AppStyles.comboBoxStyle()); cb.setPrefWidth(180);
        bar.getChildren().addAll(sb,cb);
        return bar;
    }

    private VBox buildTable() {
        VBox tc = new VBox(0); tc.setStyle("-fx-background-color:white;");
        HBox hdr = new HBox(); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setPadding(new Insets(14,32,14,32));
        hdr.setStyle("-fx-background-color:#FAFAFA;-fx-border-color:transparent transparent #E8EEEA transparent;");
        hdr.getChildren().addAll(AppStyles.colHeader("MÃ ĐƠN",120),AppStyles.colHeader("MÃ YC GỐC",120),
            AppStyles.colHeader("SITE",160),AppStyles.colHeader("MẶT HÀNG",200),
            AppStyles.colHeader("NGÀY TẠO",120),AppStyles.colHeader("TRẠNG THÁI",150),AppStyles.colHeader("THAO TÁC",120));
        tc.getChildren().add(hdr);
        List<Order> orders = orderDAO.findAll();
        for (Order o : orders) tc.getChildren().add(buildDataRow(o));
        return tc;
    }

    private HBox buildDataRow(Order order) {
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14,32,14,32));
        row.setStyle(AppStyles.rowNormal()+"-fx-cursor:hand;");
        row.setOnMouseEntered(e->row.setStyle(AppStyles.rowHover()+"-fx-cursor:hand;"));
        row.setOnMouseExited(e->row.setStyle(AppStyles.rowNormal()+"-fx-cursor:hand;"));

        Label lm = new Label(String.format("DH-2026-%03d",order.getId())); lm.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;"); lm.setMinWidth(120);
        Label ly = new Label(String.format("YC-2026-%03d",order.getRequestId())); ly.setStyle("-fx-font-size:13px;-fx-text-fill:#2E6F40;-fx-font-weight:bold;"); ly.setMinWidth(120);
        Site site = siteDAO.findById(order.getSiteId());
        Label ls = new Label(site!=null?site.getName():"Site #"+order.getSiteId()); ls.setStyle("-fx-font-size:13px;-fx-text-fill:#3A4A40;"); ls.setMinWidth(160);
        List<OrderMerchandise> items = orderDAO.findItemsByOrderId(order.getId());
        String is = items.stream().map(om->{Merchandise m=merchandiseDAO.findById(om.getMerchandiseId());return m!=null?m.getCode():"?";}).collect(Collectors.joining(", "));
        if(is.isEmpty())is="—";
        Label li = new Label(is); li.setStyle("-fx-font-size:13px;-fx-text-fill:#3A4A40;"); li.setMinWidth(200); li.setMaxWidth(200);
        String dt = order.getCreatedAt()!=null?order.getCreatedAt().toLocalDate().format(DATE_FMT):"";
        Label ld = new Label(dt); ld.setStyle("-fx-font-size:13px;-fx-text-fill:#3A4A40;"); ld.setMinWidth(120);
        HBox sb = StatusBadgeFactory.buildStatusDot(order.getStatus()!=null?order.getStatus():""); sb.setMinWidth(150);
        Button db = new Button("Chi tiết"); db.setStyle(AppStyles.btnOutline());
        db.setOnMouseEntered(e->db.setStyle(AppStyles.btnOutlineHover()));
        db.setOnMouseExited(e->db.setStyle(AppStyles.btnOutline()));
        db.setOnAction(e->navigator.showView("order-detail:"+order.getId()));
        row.getChildren().addAll(lm,ly,ls,li,ld,sb,db);
        return row;
    }

    private VBox buildPagination() {
        VBox w = new VBox(12); w.setAlignment(Pos.CENTER);
        w.setPadding(new Insets(20,32,24,32)); w.setStyle("-fx-background-color:white;");
        int sz = orderDAO.findAll().size();
        Label info = new Label("Hiển thị 1 - "+sz+" của "+sz+" đơn hàng");
        info.setStyle("-fx-font-size:13px;-fx-text-fill:#6B7C72;");
        HBox pr = new HBox(6); pr.setAlignment(Pos.CENTER);
        Button p1 = new Button("1"); p1.setStyle("-fx-background-color:#253D2C;-fx-text-fill:white;-fx-background-radius:6;-fx-font-weight:bold;-fx-padding:5 12;");
        pr.getChildren().add(p1);
        w.getChildren().addAll(info,pr);
        return w;
    }

    public Node getView() { return view; }
}
