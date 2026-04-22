package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.itss.prj_itss.common.AppStyles;
import org.itss.prj_itss.common.StatusBadgeFactory;
import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IRequestDAO;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.layout.Navigator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceivedRequestsView {

    private final BorderPane view;
    private final Navigator navigator;
    private final DAOFactory dao;
    private final IRequestDAO requestDAO;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReceivedRequestsView(Navigator navigator, DAOFactory dao) {
        this.navigator = navigator;
        this.dao = dao;
        this.requestDAO = dao.getRequestDAO();
        view = new BorderPane();
        view.getStyleClass().add("content-area");
        buildView();
    }

    private void buildView() {
        VBox header = new VBox(6);
        header.setStyle(AppStyles.pageHeaderStyle());
        Label pageTitle = new Label("Yêu cầu đặt hàng đã nhận");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        header.getChildren().add(pageTitle);
        view.setTop(header);

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: white;");
        content.getChildren().addAll(buildFilterBar(), buildTable(), buildPagination());

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: white; -fx-background: white;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        view.setCenter(sp);
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(20, 32, 20, 32));
        bar.setStyle(AppStyles.filterBarStyle());
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(AppStyles.searchBoxStyle());
        searchBox.setPrefWidth(280); searchBox.setMinHeight(40);
        Label icon = new Label("🔍"); icon.setStyle("-fx-font-size: 13px;");
        TextField tf = new TextField(); tf.setPromptText("Tìm mã yêu cầu...");
        tf.setStyle(AppStyles.searchFieldStyle());
        HBox.setHgrow(tf, Priority.ALWAYS);
        searchBox.getChildren().addAll(icon, tf);
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("Mọi trạng thái","Chờ xử lý","Đang xử lý","Đang giao","Đã hoàn thành","Đã hủy");
        cb.setValue("Mọi trạng thái"); cb.setStyle(AppStyles.comboBoxStyle()); cb.setPrefWidth(180);
        bar.getChildren().addAll(searchBox, cb);
        return bar;
    }

    private VBox buildTable() {
        VBox tc = new VBox(0);
        tc.setStyle("-fx-background-color: white;");
        HBox hdr = new HBox();
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setPadding(new Insets(14, 32, 14, 32));
        hdr.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: transparent transparent #E8EEEA transparent;");
        hdr.getChildren().addAll(
            AppStyles.colHeader("MÃ YC",120), AppStyles.colHeader("NGÀY TẠO",120),
            AppStyles.colHeader("SỐ LOẠI HÀNG",110), AppStyles.colHeader("NGÀY CẦN GẤP NHẤT",160),
            AppStyles.colHeader("TRẠNG THÁI",160), AppStyles.colHeader("THAO TÁC",200));
        tc.getChildren().add(hdr);
        List<Request> reqs = requestDAO.findAll();
        for (Request r : reqs) tc.getChildren().add(buildDataRow(r));
        return tc;
    }

    private HBox buildDataRow(Request req) {
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 32, 16, 32));
        row.setStyle(AppStyles.rowNormal()+"-fx-cursor:hand;");
        row.setOnMouseEntered(e->row.setStyle(AppStyles.rowHover()+"-fx-cursor:hand;"));
        row.setOnMouseExited(e->row.setStyle(AppStyles.rowNormal()+"-fx-cursor:hand;"));
        String maYC = String.format("YC-2026-%03d", req.getId());
        Label m = new Label(maYC); m.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;"); m.setMinWidth(120);
        String d = req.getCreatedAt()!=null? req.getCreatedAt().toLocalDate().format(DATE_FMT):"";
        Label ld = new Label(d); ld.setStyle("-fx-font-size:13px;-fx-text-fill:#3A4A40;"); ld.setMinWidth(120);
        Label li = new Label(requestDAO.countItemTypes(req.getId())+" loại"); li.setStyle("-fx-font-size:13px;-fx-text-fill:#3A4A40;"); li.setMinWidth(110);
        LocalDate earliest = requestDAO.getEarliestDeliveryDate(req.getId());
        Label lg = new Label(earliest!=null?earliest.format(DATE_FMT):"N/A"); lg.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#D84315;"); lg.setMinWidth(160);
        HBox sb = StatusBadgeFactory.buildStatusDot(req.getStatus()!=null?req.getStatus():""); sb.setMinWidth(160);
        HBox ab = new HBox(8); ab.setAlignment(Pos.CENTER_LEFT);
        Button eye = new Button("Chi tiết"); eye.setStyle(AppStyles.btnOutline());
        eye.setOnMouseEntered(e->eye.setStyle(AppStyles.btnOutlineHover()));
        eye.setOnMouseExited(e->eye.setStyle(AppStyles.btnOutline()));
        eye.setOnAction(e->RequestDetailPopup.show(view.getScene()==null?null:view.getScene().getWindow(),maYC,dao));
        ab.getChildren().add(eye);
        if("Chờ xử lý".equals(req.getStatus())){
            Button pb = new Button("⚙ Xử lý"); pb.setStyle(AppStyles.btnDark());
            pb.setOnMouseEntered(e->pb.setStyle(AppStyles.btnDarkHover()));
            pb.setOnMouseExited(e->pb.setStyle(AppStyles.btnDark()));
            pb.setOnAction(e->navigator.showView("request-processing"));
            ab.getChildren().add(pb);
        }
        HBox.setHgrow(ab, Priority.ALWAYS);
        row.getChildren().addAll(m,ld,li,lg,sb,ab);
        return row;
    }

    private VBox buildPagination() {
        VBox w = new VBox(12); w.setAlignment(Pos.CENTER);
        w.setPadding(new Insets(20,32,24,32)); w.setStyle("-fx-background-color:white;");
        int sz = requestDAO.findAll().size();
        Label info = new Label("Hiển thị 1 - "+sz+" của "+sz+" yêu cầu");
        info.setStyle("-fx-font-size:13px;-fx-text-fill:#6B7C72;");
        HBox pr = new HBox(6); pr.setAlignment(Pos.CENTER);
        Button p1 = new Button("1");
        p1.setStyle("-fx-background-color:#253D2C;-fx-text-fill:white;-fx-background-radius:6;-fx-font-weight:bold;-fx-padding:5 12;");
        pr.getChildren().add(p1);
        w.getChildren().addAll(info,pr);
        return w;
    }

    public Node getView() { return view; }
}
