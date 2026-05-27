package org.itss.prj_itss.view.ordering.site;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.site.SiteManagementController;
import org.itss.prj_itss.model.site.application.SiteRow;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.List;
import java.util.Locale;

public final class SiteManagementView implements ViewLifecycle {

    private final ObservableList<SiteRow> siteRows = FXCollections.observableArrayList();
    private final FilteredList<SiteRow> filteredRows = new FilteredList<>(siteRows, row -> true);

    private Navigator navigator;
    private SiteManagementController controller;

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

    public void init(Navigator navigator, SiteManagementController controller) {
        this.navigator = navigator;
        this.controller = controller;
        reload();
    }

    @Override
    public void onViewShown() {
        reload();
    }

    private void reload() {
        if (controller == null) return;
        List<Site> sites = controller.getSites();
        siteRows.setAll(sites.stream().map(controller::toRow).toList());

        totalSitesLabel.setText(String.valueOf(sites.size()));
        activeSitesLabel.setText(String.valueOf(sites.size()));
        merchandiseCountLabel.setText(String.valueOf(controller.countMerchandise()));

        applyFilter();
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
}
