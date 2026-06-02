package org.itss.prj_itss.view.admin.account;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleStringProperty;
import org.itss.prj_itss.controller.admin.account.AccountManagementController;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.model.auth.application.management.AccountDraft;
import org.itss.prj_itss.model.auth.application.management.AccountManagementResult;
import org.itss.prj_itss.model.auth.application.management.AccountManagementSnapshot;
import org.itss.prj_itss.model.auth.application.management.AccountRow;
import org.itss.prj_itss.model.auth.domain.Role;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import javafx.event.ActionEvent;
import java.util.List;
import java.util.Optional;

public final class AccountManagementView implements ViewLifecycle {

    private Navigator navigator;
    private AccountManagementController controller;
    private List<AccountRow> allRows = List.of();
    private List<Role> assignableRoles = List.of();
    private final ObservableList<AccountRow> displayedRows = FXCollections.observableArrayList();

    @FXML private Label totalAccountsLabel;
    @FXML private Label activeAccountsLabel;
    @FXML private Label disabledAccountsLabel;
    @FXML private TextField searchField;
    @FXML private Button createAccountButton;
    @FXML private Button editAccountButton;
    @FXML private Button disableAccountButton;
    @FXML private Button deleteAccountButton;
    @FXML private TableView<AccountRow> accountTable;
    @FXML private TableColumn<AccountRow, String> usernameColumn;
    @FXML private TableColumn<AccountRow, String> fullNameColumn;
    @FXML private TableColumn<AccountRow, String> roleColumn;
    @FXML private TableColumn<AccountRow, String> statusColumn;

    @FXML
    private void initialize() {
        accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        usernameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().username()));
        fullNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
        roleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().roleName()));
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        accountTable.setItems(displayedRows);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        createAccountButton.setOnAction(e -> handleCreate());
        editAccountButton.setOnAction(e -> handleEdit());
        disableAccountButton.setOnAction(e -> handleDisable());
        deleteAccountButton.setOnAction(e -> handleDelete());
    }

    public void init(Navigator navigator, AccountManagementController controller) {
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
        AccountManagementSnapshot snapshot = controller.load();
        allRows = snapshot.rows();
        assignableRoles = snapshot.assignableRoles();
        totalAccountsLabel.setText(String.valueOf(allRows.size()));
        activeAccountsLabel.setText(String.valueOf(snapshot.activeCount()));
        disabledAccountsLabel.setText(String.valueOf(snapshot.disabledCount()));
        applyFilter();
    }

    private void applyFilter() {
        String keyword = searchField.getText();
        List<AccountRow> filtered = controller != null
            ? controller.filterRows(allRows, keyword)
            : allRows;
        displayedRows.setAll(filtered);
    }

    private void handleCreate() {
        if (controller == null) return;
        Optional<AccountDraft> draft = buildAccountDialog("Tạo tài khoản", null, assignableRoles);
        draft.ifPresent(d -> {
            AccountManagementResult result = controller.create(d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private void handleEdit() {
        if (controller == null) return;
        AccountRow selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn tài khoản cần sửa.");
            return;
        }
        Optional<AccountDraft> draft = buildAccountDialog("Sửa tài khoản", selected, assignableRoles);
        draft.ifPresent(d -> {
            AccountManagementResult result = controller.update(selected.accountId(), d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private void handleDisable() {
        if (controller == null) return;
        AccountRow selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn tài khoản cần vô hiệu hóa.");
            return;
        }
        if (!confirm("Xác nhận vô hiệu hóa", "Vô hiệu hóa tài khoản \"" + selected.username() + "\"?")) return;
        AccountManagementResult result = controller.disable(selected.accountId());
        showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
            result.success() ? "Thành công" : "Lỗi", result.message());
        if (result.success()) reload();
    }

    private void handleDelete() {
        if (controller == null) return;
        AccountRow selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn tài khoản cần hủy.");
            return;
        }
        if (!confirm("Xác nhận hủy tài khoản", "Hủy tài khoản \"" + selected.username() + "\"? Thao tác không thể khôi phục.")) return;
        AccountManagementResult result = controller.delete(selected.accountId());
        showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
            result.success() ? "Thành công" : "Lỗi", result.message());
        if (result.success()) reload();
    }

    private Optional<AccountDraft> buildAccountDialog(String title, AccountRow prefill, List<Role> roles) {
        Dialog<AccountDraft> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Họ và tên");
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(roles);
        roleCombo.setConverter(new javafx.util.StringConverter<Role>() {
            @Override public String toString(Role r) { return r == null ? "" : r.getName(); }
            @Override public Role fromString(String s) { return null; }
        });

        if (prefill != null) {
            usernameField.setText(prefill.username());
            fullNameField.setText(prefill.fullName());
            roles.stream().filter(r -> r.getName().equals(prefill.roleName())).findFirst()
                .ifPresent(roleCombo::setValue);
        }
        if (!roles.isEmpty() && roleCombo.getValue() == null) {
            roleCombo.setValue(roles.get(0));
        }

        grid.add(new Label("Tên đăng nhập:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Họ và tên:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Vai trò:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
            String password = passwordField.getText() == null ? "" : passwordField.getText();
            String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
            Role role = roleCombo.getValue();
            if (username.isBlank() || password.isBlank() || fullName.isBlank() || role == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ thông tin.");
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String fullName = fullNameField.getText().trim();
            Role role = roleCombo.getValue();
            return new AccountDraft(username, password, fullName, role.getId(), null);
        });

        return dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
