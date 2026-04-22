package org.itss.prj_itss.order;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

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
import org.itss.prj_itss.layout.ViewController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderDetailController implements ViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Navigator navigator;
    private IOrderDAO orderDAO;
    private ISiteDAO siteDAO;
    private IMerchandiseDAO merchandiseDAO;

    private String orderId;

    @FXML
    private Label orderCodeLabel;

    @FXML
    private HBox statusBadgeContainer;

    @FXML
    private Label requestCodeValueLabel;

    @FXML
    private Label siteValueLabel;

    @FXML
    private Label createdAtValueLabel;

    @FXML
    private Label statusValueLabel;

    @FXML
    private TableView<OrderItemRow> itemsTable;

    @FXML
    private TableColumn<OrderItemRow, String> merchandiseCodeColumn;

    @FXML
    private TableColumn<OrderItemRow, String> merchandiseNameColumn;

    @FXML
    private TableColumn<OrderItemRow, String> quantityColumn;

    @FXML
    private TableColumn<OrderItemRow, String> transportColumn;

    @FXML
    private void initialize() {
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        merchandiseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseCode()));
        merchandiseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseName()));
        quantityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().quantity()));
        transportColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().transport()));
    }

    @Override
    public void init(Navigator navigator, DAOFactory daoFactory) {
        this.navigator = navigator;
        this.orderDAO = daoFactory.getOrderDAO();
        this.siteDAO = daoFactory.getSiteDAO();
        this.merchandiseDAO = daoFactory.getMerchandiseDAO();
        loadIfReady();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
        loadIfReady();
    }

    @FXML
    private void goBack() {
        if (navigator != null) {
            navigator.showView("orders");
        }
    }

    private void loadIfReady() {
        if (orderDAO == null || orderId == null || orderId.isBlank()) {
            return;
        }

        int parsedOrderId = parseOrderId(orderId);
        Order order = orderDAO.findById(parsedOrderId);

        if (order == null) {
            orderCodeLabel.setText("Không tìm thấy đơn hàng");
            statusBadgeContainer.getChildren().clear();
            requestCodeValueLabel.setText("N/A");
            siteValueLabel.setText("N/A");
            createdAtValueLabel.setText("N/A");
            statusValueLabel.setText("N/A");
            itemsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        Site site = siteDAO.findById(order.getSiteId());
        String status = order.getStatus() == null ? "N/A" : order.getStatus();

        orderCodeLabel.setText(String.format("DH-2026-%03d", order.getId()));
        statusBadgeContainer.getChildren().setAll(StatusBadgeFactory.buildStatusBadge(status));
        requestCodeValueLabel.setText(String.format("YC-2026-%03d", order.getRequestId()));
        siteValueLabel.setText(site == null ? "Site #" + order.getSiteId() : site.getName());
        createdAtValueLabel.setText(order.getCreatedAt() == null ? "N/A" : order.getCreatedAt().toLocalDate().format(DATE_FORMAT));
        statusValueLabel.setText(status);

        List<OrderItemRow> items = orderDAO.findItemsByOrderId(order.getId()).stream()
            .map(this::toItemRow)
            .toList();
        itemsTable.setItems(FXCollections.observableArrayList(items));
    }

    private OrderItemRow toItemRow(OrderMerchandise item) {
        Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId());
        return new OrderItemRow(
            merchandise == null ? "N/A" : merchandise.getCode(),
            merchandise == null ? "N/A" : merchandise.getName(),
            item.getQuantity() == null ? "0" : item.getQuantity().toPlainString(),
            item.getDeliveryMethod() == null ? "N/A" : item.getDeliveryMethod()
        );
    }

    private int parseOrderId(String rawValue) {
        try {
            return Integer.parseInt(rawValue.replaceAll("\\D+", ""));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private record OrderItemRow(
        String merchandiseCode,
        String merchandiseName,
        String quantity,
        String transport
    ) { }
}
