package org.itss.prj_itss.site;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.service.MerchandiseService;
import org.itss.prj_itss.service.SiteService;

import java.util.List;
import java.util.Locale;

public class SiteManagementController implements IViewController {

    private final ObservableList<SiteRow> siteRows = FXCollections.observableArrayList();
    private final FilteredList<SiteRow> filteredRows = new FilteredList<>(siteRows, row -> true);

    private SiteService siteService;
    private MerchandiseService merchandiseService;

    @FXML
    private Label totalSitesLabel;

    @FXML
    private Label activeSitesLabel;

    @FXML
    private Label merchandiseCountLabel;

    @FXML
    private Label siteCountLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button addSiteButton;

    @FXML
    private TableView<SiteRow> siteTable;

    @FXML
    private TableColumn<SiteRow, String> siteCodeColumn;

    @FXML
    private TableColumn<SiteRow, String> siteNameColumn;

    @FXML
    private TableColumn<SiteRow, String> descriptionColumn;

    @FXML
    private TableColumn<SiteRow, String> shipDaysColumn;

    @FXML
    private TableColumn<SiteRow, String> airDaysColumn;

    @FXML
    private TableColumn<SiteRow, String> itemCountColumn;

    @FXML
    private void initialize() {
        siteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().description()));
        shipDaysColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().shipDays()));
        airDaysColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().airDays()));
        itemCountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemCount()));

        siteTable.setItems(filteredRows);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        addSiteButton.setOnAction(event -> showAddSiteNotice());
    }

    @Override
    public void init(INavigator navigator, ApplicationContext context) {
        this.siteService = context.siteService();
        this.merchandiseService = context.merchandiseService();
        reload();
    }

    private void reload() {
        List<Site> sites = siteService.findAll();
        siteRows.setAll(sites.stream().map(this::toRow).toList());

        totalSitesLabel.setText(String.valueOf(sites.size()));
        activeSitesLabel.setText(String.valueOf(sites.size()));
        merchandiseCountLabel.setText(String.valueOf(merchandiseService.countAll()));

        applyFilter();
    }

    private SiteRow toRow(Site site) {
        int itemCount = siteService.countMerchandiseAtSite(site.getId());
        return new SiteRow(
            site.getSiteCode(),
            site.getName(),
            site.getDescription() == null || site.getDescription().isBlank() ? "-" : site.getDescription(),
            formatDays(site.getShipDeliveryDays()),
            formatDays(site.getAirDeliveryDays()),
            String.valueOf(itemCount)
        );
    }

    private String formatDays(Integer days) {
        return days == null ? "N/A" : days + " ngày";
    }

    private void applyFilter() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        filteredRows.setPredicate(row ->
            keyword.isBlank()
                || row.siteCode().toLowerCase(Locale.ROOT).contains(keyword)
                || row.siteName().toLowerCase(Locale.ROOT).contains(keyword)
                || row.description().toLowerCase(Locale.ROOT).contains(keyword)
        );

        siteCountLabel.setText(filteredRows.size() + " site");
    }

    private void showAddSiteNotice() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Chưa triển khai");
        alert.setContentText("Màn hình thêm site mới sẽ được tách tiếp theo trong luồng MVC.");
        alert.showAndWait();
    }

    private record SiteRow(
        String siteCode,
        String siteName,
        String description,
        String shipDays,
        String airDays,
        String itemCount
    ) { }
}
