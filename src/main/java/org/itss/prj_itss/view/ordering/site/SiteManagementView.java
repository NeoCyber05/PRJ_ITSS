package org.itss.prj_itss.view.ordering.site;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.site.SiteManagementController;
import org.itss.prj_itss.model.site.application.SiteAccountDraft;
import org.itss.prj_itss.model.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.model.site.application.SiteManagementResult;
import org.itss.prj_itss.model.site.application.SiteDraft;
import org.itss.prj_itss.model.site.application.SiteRow;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.List;
import java.util.Optional;

public final class SiteManagementView implements ViewLifecycle {

    private final ObservableList<SiteRow> displayedRows = FXCollections.observableArrayList();
    private List<SiteRow> allRows = List.of();

    private Navigator navigator;
    private SiteManagementController controller;

    @FXML private Label totalSitesLabel;
    @FXML private Label activeSitesLabel;
    @FXML private Label merchandiseCountLabel;
    @FXML private Label siteCountLabel;
    @FXML private TextField searchField;
    @FXML private Button addSiteButton;
    @FXML private Button editSiteButton;
    @FXML private Button createSiteAccountButton;
    @FXML private TableView<SiteRow> siteTable;
    @FXML private TableColumn<SiteRow, String> siteCodeColumn;
    @FXML private TableColumn<SiteRow, String> siteNameColumn;
    @FXML private TableColumn<SiteRow, String> descriptionColumn;
    @FXML private TableColumn<SiteRow, String> shipDaysColumn;
    @FXML private TableColumn<SiteRow, String> airDaysColumn;
    @FXML private TableColumn<SiteRow, String> itemCountColumn;

    @FXML
    private void initialize() {
        siteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().description()));
        shipDaysColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().shipDays()));
        airDaysColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().airDays()));
        itemCountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemCount()));
        siteTable.setItems(displayedRows);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        addSiteButton.setOnAction(e -> handleAddSite());
        editSiteButton.setOnAction(e -> handleEditSite());
        createSiteAccountButton.setOnAction(e -> handleCreateSiteAccount());
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
        SiteManagementApplicationService.Snapshot snapshot = controller.load();
        allRows = snapshot.rows();
        totalSitesLabel.setText(String.valueOf(snapshot.totalSites()));
        activeSitesLabel.setText(String.valueOf(snapshot.totalSites())); // no site.status in schema
        merchandiseCountLabel.setText(String.valueOf(snapshot.merchandiseCount()));
        applyFilter();
    }

    private void applyFilter() {
        String keyword = searchField.getText();
        List<SiteRow> filtered = controller != null
            ? controller.filterRows(allRows, keyword)
            : allRows;
        displayedRows.setAll(filtered);
        siteCountLabel.setText(filtered.size() + " site");
    }

    private void handleAddSite() {
        if (controller == null) return;
        Optional<SiteDraft> draft = buildSiteDialog("Thêm Site mới", null);
        draft.ifPresent(d -> {
            SiteManagementResult result = controller.createSite(d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private void handleEditSite() {
        if (controller == null) return;
        SiteRow selected = siteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn site cần sửa.");
            return;
        }
        Optional<SiteDraft> draft = buildSiteDialog("Sửa Site", selected);
        draft.ifPresent(d -> {
            SiteManagementResult result = controller.updateSite(selected.siteId(), d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private void handleCreateSiteAccount() {
        if (controller == null) return;
        SiteRow selected = siteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn site cần tạo tài khoản.");
            return;
        }
        Optional<SiteAccountDraft> draft = buildSiteAccountDialog(selected.siteName());
        draft.ifPresent(d -> {
            SiteManagementResult result = controller.provisionSiteAccount(selected.siteId(), d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private Optional<SiteDraft> buildSiteDialog(String title, SiteRow prefill) {
        Dialog<SiteDraft> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField siteCodeField = new TextField();
        siteCodeField.setPromptText("Mã site (VD: TOKYO)");
        TextField nameField = new TextField();
        nameField.setPromptText("Tên site");
        TextField descField = new TextField();
        descField.setPromptText("Mô tả (tùy chọn)");
        TextField shipDaysField = new TextField();
        shipDaysField.setPromptText("Số ngày vận chuyển biển");
        TextField airDaysField = new TextField();
        airDaysField.setPromptText("Số ngày vận chuyển hàng không");

        if (prefill != null) {
            siteCodeField.setText(prefill.siteCode());
            nameField.setText(prefill.siteName());
            descField.setText(prefill.description() != null && !prefill.description().equals("-") ? prefill.description() : "");
            if (prefill.site() != null) {
                Integer ship = prefill.site().getShipDeliveryDays();
                Integer air = prefill.site().getAirDeliveryDays();
                if (ship != null) shipDaysField.setText(String.valueOf(ship));
                if (air != null) airDaysField.setText(String.valueOf(air));
            }
        }

        grid.add(new Label("Mã site:"), 0, 0); grid.add(siteCodeField, 1, 0);
        grid.add(new Label("Tên site:"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Mô tả:"), 0, 2); grid.add(descField, 1, 2);
        grid.add(new Label("Ngày vận chuyển biển:"), 0, 3); grid.add(shipDaysField, 1, 3);
        grid.add(new Label("Ngày vận chuyển hàng không:"), 0, 4); grid.add(airDaysField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (siteCodeField.getText().trim().isBlank() || nameField.getText().trim().isBlank()) {
                    showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Mã site và tên site không được để trống.");
                    event.consume();
                }
            });
        }

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String siteCode = siteCodeField.getText().trim();
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            Integer shipDays = parseOptionalInt(shipDaysField.getText());
            Integer airDays = parseOptionalInt(airDaysField.getText());
            return new SiteDraft(siteCode, name, desc.isEmpty() ? null : desc, shipDays, airDays);
        });

        return dialog.showAndWait();
    }

    private Optional<SiteAccountDraft> buildSiteAccountDialog(String siteName) {
        Dialog<SiteAccountDraft> dialog = new Dialog<>();
        dialog.setTitle("Tạo tài khoản Site: " + siteName);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Họ và tên");

        grid.add(new Label("Tên đăng nhập:"), 0, 0); grid.add(usernameField, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1); grid.add(passwordField, 1, 1);
        grid.add(new Label("Họ và tên:"), 0, 2); grid.add(fullNameField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (usernameField.getText().trim().isBlank()
                        || passwordField.getText().isBlank()
                        || fullNameField.getText().trim().isBlank()) {
                    showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ thông tin.");
                    event.consume();
                }
            });
        }

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            return new SiteAccountDraft(
                usernameField.getText().trim(),
                passwordField.getText(),
                fullNameField.getText().trim()
            );
        });

        return dialog.showAndWait();
    }

    private Integer parseOptionalInt(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try { return Integer.parseInt(text.trim()); } catch (NumberFormatException e) { return null; }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
