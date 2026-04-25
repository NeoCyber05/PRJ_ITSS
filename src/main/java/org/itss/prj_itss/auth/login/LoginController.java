package org.itss.prj_itss.auth.login;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import org.itss.prj_itss.auth.AuthenticatedUser;
import org.itss.prj_itss.service.AuthenticationService;

import java.util.function.Consumer;

public class LoginController {

    private AuthenticationService authenticationService;
    private Consumer<AuthenticatedUser> loginHandler;
    private final ProgressIndicator loadingIndicator = createLoadingIndicator();
    private boolean loading;
    private boolean startupConnecting;
    private boolean startupConnectionFailed;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        loginButton.setContentDisplay(ContentDisplay.LEFT);
        loginButton.setGraphicTextGap(10);
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearMessage();
            refreshActionState();
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearMessage();
            refreshActionState();
        });
        clearMessage();
        refreshActionState();
        Platform.runLater(usernameField::requestFocus);
    }

    public void configure(AuthenticationService authenticationService, Consumer<AuthenticatedUser> loginHandler) {
        this.authenticationService = authenticationService;
        this.loginHandler = loginHandler;
    }

    public void beginStartupConnection() {
        startupConnecting = true;
        startupConnectionFailed = false;
        refreshLoginButtonState();
    }

    public void completeStartupConnection() {
        startupConnecting = false;
        startupConnectionFailed = false;
        refreshLoginButtonState();
    }

    public void failStartupConnection() {
        startupConnecting = false;
        startupConnectionFailed = true;
        showMessage("Không thể kết nối cơ sở dữ liệu.", false);
        refreshLoginButtonState();
    }

    @FXML
    private void handleLogin() {
        if (loading || startupConnecting || startupConnectionFailed) {
            return;
        }
        if (authenticationService == null || loginHandler == null) {
            showMessage("Ứng dụng chưa sẵn sàng để đăng nhập.", false);
            return;
        }

        setLoading(true);
        clearMessage();

        String username = usernameField.getText();
        String password = passwordField.getText();

        Task<LoginResult> authenticateTask = new Task<>() {
            @Override
            protected LoginResult call() {
                return authenticationService.authenticate(username, password);
            }
        };

        authenticateTask.setOnSucceeded(event -> {
            try {
                LoginResult result = authenticateTask.getValue();
                if (!result.success()) {
                    showMessage(result.message(), false);
                    return;
                }
                loginHandler.accept(result.user());
            } finally {
                setLoading(false);
            }
        });

        authenticateTask.setOnFailed(event -> {
            try {
                showMessage("Không thể kiểm tra tài khoản. Vui lòng kiểm tra kết nối cơ sở dữ liệu.", false);
            } finally {
                setLoading(false);
            }
        });

        Thread authenticationThread = new Thread(authenticateTask, "login-authenticate");
        authenticationThread.setDaemon(true);
        authenticationThread.start();
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        refreshLoginButtonState();
    }

    private ProgressIndicator createLoadingIndicator() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        indicator.setPrefSize(16, 16);
        indicator.setMinSize(16, 16);
        indicator.setMaxSize(16, 16);
        indicator.getStyleClass().add("login-loading-indicator");
        return indicator;
    }

    private void refreshActionState() {
        boolean disableButton = loading
            || startupConnecting
            || startupConnectionFailed
            || usernameField.getText() == null
            || usernameField.getText().trim().isBlank()
            || passwordField.getText() == null
            || passwordField.getText().isBlank();
        loginButton.setDisable(disableButton);
    }

    private void refreshLoginButtonState() {
        if (loading) {
            loginButton.setText("Đang đăng nhập...");
            loginButton.setGraphic(loadingIndicator);
        } else if (startupConnecting) {
            loginButton.setText("Đang kết nối...");
            loginButton.setGraphic(loadingIndicator);
        } else if (startupConnectionFailed) {
            loginButton.setText("Không thể kết nối");
            loginButton.setGraphic(null);
        } else {
            loginButton.setText("Đăng nhập");
            loginButton.setGraphic(null);
        }
        refreshActionState();
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.getStyleClass().removeAll("login-feedback-error", "login-feedback-success");
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
        messageLabel.getStyleClass().removeAll("login-feedback-error", "login-feedback-success");
        messageLabel.getStyleClass().add(success ? "login-feedback-success" : "login-feedback-error");
    }
}
