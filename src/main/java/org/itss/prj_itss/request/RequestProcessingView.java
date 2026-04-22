package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.itss.prj_itss.common.AppStyles;
import org.itss.prj_itss.common.ToastHelper;
import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IRequestDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.dao.IInventoryDAO;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.request.RequestModels.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RequestProcessingView {

    private final BorderPane view;
    private final Navigator navigator;
    private final int requestId;

    private final IRequestDAO requestDAO;
    private final ISiteDAO siteDAO;
    private final IInventoryDAO inventoryDAO;
    private final IMerchandiseDAO merchandiseDAO;

    private final List<ItemReq> items = new ArrayList<>();
    private final List<SiteInfo> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();

    private SiteFilterSection siteFilter;
    private AllocationSection allocationSection;
    private Label[] allocFractionLabels;
    private int deadlineDays = 14;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RequestProcessingView(Navigator navigator, DAOFactory dao, int requestId) {
        this.navigator = navigator;
        this.requestId = requestId;
        this.requestDAO = dao.getRequestDAO();
        this.siteDAO = dao.getSiteDAO();
        this.inventoryDAO = dao.getInventoryDAO();
        this.merchandiseDAO = dao.getMerchandiseDAO();

        view = new BorderPane();
        view.setStyle("-fx-background-color: #F5F9F6;");
        loadDataFromDB();
        allocFractionLabels = new Label[items.size()];
        buildView();
    }

    private void loadDataFromDB() {
        List<RequestMerchandise> rmList = requestDAO.findItemsByRequestId(requestId);
        for (RequestMerchandise rm : rmList) {
            Merchandise m = merchandiseDAO.findById(rm.getMerchandiseId());
            if (m != null) items.add(new ItemReq(m.getId(), m.getCode(), m.getName(), rm.getQuantityOrdered().intValue()));
        }
        LocalDate earliest = requestDAO.getEarliestDeliveryDate(requestId);
        if (earliest != null) { deadlineDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), earliest); if (deadlineDays < 1) deadlineDays = 1; }

        List<Site> sites = siteDAO.findAll();
        for (Site site : sites) {
            Map<Integer, Integer> inv = inventoryDAO.getInventoryBySiteId(site.getId());
            allSites.add(new SiteInfo(site.getId(), site.getSiteCode(), site.getName(), site.getDescription(),
                site.getShipDeliveryDays() != null ? site.getShipDeliveryDays() : 999,
                site.getAirDeliveryDays() != null ? site.getAirDeliveryDays() : 999, inv));
        }
        for (ItemReq item : items) allocations.put(item.merchandiseId, new LinkedHashMap<>());
    }

    private void buildView() {
        view.setTop(buildHeader());
        siteFilter = new SiteFilterSection(allSites);
        allocationSection = new AllocationSection(items, allSites, siteFilter.getExcludedSiteIds(), siteFilter.getPrioritySiteIds(), allocations, deadlineDays);
        allocationSection.setAllocFractionLabels(allocFractionLabels);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 28, 20, 28));
        content.setStyle("-fx-background-color: #F5F9F6;");
        content.getChildren().addAll(siteFilter.build(), buildItemsTable(), allocationSection.buildWrapper());

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true); sp.setStyle("-fx-background-color: #F5F9F6; -fx-background: #F5F9F6;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        view.setCenter(sp);
        view.setBottom(buildBottomBar());
    }

    private HBox buildHeader() {
        HBox hdr = new HBox(); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setPadding(new Insets(18,28,18,28));
        hdr.setStyle("-fx-background-color:white;-fx-border-color:transparent transparent #D8E8DD transparent;-fx-border-width:0 0 1 0;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),4,0,0,2);");
        VBox left = new VBox(3);
        Label sec = new Label("XỬ LÝ YÊU CẦU"); sec.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#2E6F40;");
        String code = String.format("YC-2026-%03d", requestId);
        Label t = new Label("Yêu cầu "+code); t.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        LocalDate earliest = requestDAO.getEarliestDeliveryDate(requestId);
        int totalQty = items.stream().mapToInt(i->i.required).sum();
        Label desc = new Label("Ngày cần giao: "+(earliest!=null?earliest.format(DATE_FMT):"N/A")+"  •  "+items.size()+" mặt hàng  •  "+totalQty+" chiếc");
        desc.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        left.getChildren().addAll(sec,t,desc);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label badge = new Label("⏳ Chờ xử lý"); badge.setStyle("-fx-background-color:#FFF3E0;-fx-text-fill:#E65100;-fx-background-radius:8;-fx-padding:6 14;-fx-font-size:13px;-fx-font-weight:bold;");
        hdr.getChildren().addAll(left,sp,badge);
        return hdr;
    }

    private VBox buildItemsTable() {
        VBox card = new VBox(0); card.setStyle(AppStyles.cardStyle()); card.setPadding(Insets.EMPTY);
        HBox ch = new HBox(); ch.setPadding(new Insets(11,20,11,20));
        ch.setStyle("-fx-background-color:#F5F9F6;-fx-border-color:transparent transparent #E8EEEA transparent;-fx-border-width:0 0 1 0;");
        ch.setAlignment(Pos.CENTER_LEFT);
        ch.getChildren().addAll(AppStyles.colHeader("MÃ HÀNG",140),AppStyles.colHeader("SỐ LƯỢNG YÊU CẦU",150),
            AppStyles.colHeader("NGÀY CẦN GIAO",140),AppStyles.colHeader("ĐÃ PHÂN BỔ",160),
            new Region(){{HBox.setHgrow(this,Priority.ALWAYS);}},AppStyles.colHeader("TỔNG TỒN KHO",120));
        card.getChildren().add(ch);
        for (int i=0;i<items.size();i++) card.getChildren().add(buildItemRow(items.get(i),i));
        return card;
    }

    private HBox buildItemRow(ItemReq item, int idx) {
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14,20,14,20));
        row.setStyle("-fx-border-color:transparent transparent #F0F4F2 transparent;-fx-border-width:0 0 1 0;");
        VBox cc = new VBox(2); cc.setMinWidth(140);
        Label cl = new Label(item.code); cl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label nl = new Label(item.name); nl.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        cc.getChildren().addAll(cl,nl);
        Label rl = new Label(item.required+" Chiếc"); rl.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;"); rl.setMinWidth(150);
        LocalDate earliest = requestDAO.getEarliestDeliveryDate(requestId);
        Label dl = new Label(earliest!=null?earliest.format(DATE_FMT):"N/A"); dl.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;"); dl.setMinWidth(140);
        int alloc = allocationSection!=null?allocationSection.getAllocated(item.merchandiseId):0;
        boolean done = alloc>=item.required;
        VBox ac = new VBox(4); ac.setMinWidth(160);
        Label eb; if(done){eb=new Label("✅ Đủ");eb.setStyle("-fx-background-color:#E8F5E9;-fx-text-fill:#2E7D32;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");}
        else if(alloc>0){eb=new Label("⚠ Chưa đủ");eb.setStyle("-fx-background-color:#FFF3E0;-fx-text-fill:#E65100;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");}
        else{eb=new Label("○ Chưa phân bổ");eb.setStyle("-fx-background-color:#F0F4F2;-fx-text-fill:#8FA899;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");}
        Label fl = new Label(alloc+"/"+item.required+" chiếc"); fl.setStyle("-fx-font-size:13px;-fx-text-fill:"+(done?"#2E6F40":"#E65100")+";-fx-font-weight:bold;");
        allocFractionLabels[idx]=fl; if(allocationSection!=null)allocationSection.setAllocFractionLabels(allocFractionLabels);
        ac.getChildren().addAll(eb,fl);
        Region spacer = new Region(); HBox.setHgrow(spacer,Priority.ALWAYS);
        int ts = allSites.stream().filter(s->!siteFilter.getExcludedSiteIds().contains(s.id)).mapToInt(s->s.stock.getOrDefault(item.merchandiseId,0)).sum();
        VBox sc = new VBox(2); sc.setAlignment(Pos.CENTER_RIGHT); sc.setMinWidth(120);
        Label sv = new Label(String.valueOf(ts)); sv.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label sl = new Label("Hiện tồn kho"); sl.setStyle("-fx-font-size:11px;-fx-text-fill:#8FA899;");
        sc.getChildren().addAll(sv,sl);
        row.getChildren().addAll(cc,rl,dl,ac,spacer,sc);
        return row;
    }

    private HBox buildBottomBar() {
        HBox bar = new HBox(); bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(16,28,16,28));
        bar.setStyle("-fx-background-color:white;-fx-border-color:#D8E8DD transparent transparent transparent;-fx-border-width:1 0 0 0;");
        Button back = new Button("← Danh sách yêu cầu"); back.setStyle(AppStyles.btnSecondary());
        back.setOnAction(e->navigator.showView("received-requests"));
        Region sp = new Region(); HBox.setHgrow(sp,Priority.ALWAYS);
        Button confirm = new Button("▶ Xác nhận và gửi");
        confirm.setStyle("-fx-background-color:#253D2C;-fx-text-fill:white;-fx-background-radius:8;-fx-font-size:14px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:12 32;");
        confirm.setOnAction(e->handleConfirm());
        bar.getChildren().addAll(back,sp,confirm);
        return bar;
    }

    private void handleConfirm() {
        List<String> errors = new ArrayList<>();
        for (ItemReq it : items) {
            int a = allocationSection.getAllocated(it.merchandiseId);
            if(a<it.required) errors.add("• "+it.code+" chỉ phân bổ "+a+"/"+it.required);
            if(a>it.required) errors.add("• "+it.code+" phân bổ vượt "+a+"/"+it.required);
        }
        if(!errors.isEmpty()){
            Alert al = new Alert(Alert.AlertType.WARNING); al.setTitle("Chưa hoàn tất"); al.setHeaderText("Vui lòng kiểm tra:");
            al.setContentText(String.join("\n",errors)); ToastHelper.styleDialog(al); al.showAndWait(); return;
        }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION); c.setTitle("Xác nhận"); c.setHeaderText("Bạn có chắc muốn tạo đơn?");
        int tq = items.stream().mapToInt(i->i.required).sum();
        long sc = allocations.values().stream().flatMap(m->m.keySet().stream()).distinct().count();
        c.setContentText("Tổng: "+tq+" chiếc | "+sc+" site"); ToastHelper.styleDialog(c);
        Optional<ButtonType> r = c.showAndWait();
        if(r.isPresent()&&r.get()==ButtonType.OK){ navigator.showView("orders"); ToastHelper.showToast("🎉 Đã tạo đơn hàng thành công!"); }
    }

    public Node getView() { return view; }
}
