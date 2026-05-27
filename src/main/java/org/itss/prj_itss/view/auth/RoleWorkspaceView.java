package org.itss.prj_itss.view.auth;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.controller.auth.RoleWorkspaceController;
import org.itss.prj_itss.view.shared.ViewLifecycle;

public class RoleWorkspaceView implements ViewLifecycle {

    private RoleWorkspaceController controller;
    private AuthenticatedUser user;

    @FXML
    private Label workspaceTitleLabel;

    @FXML
    private Label workspaceSubtitleLabel;

    @FXML
    private Label workspaceSummaryLabel;

    @FXML
    private Label accessNoteLabel;

    @FXML
    private Label statusBadgeLabel;

    @FXML
    private VBox capabilityListContainer;

    public void setController(RoleWorkspaceController controller) {
        this.controller = controller;
    }

    public void setUser(AuthenticatedUser user) {
        this.user = user;
        refresh();
    }

    private void refresh() {
        if (user == null || workspaceTitleLabel == null) {
            return;
        }

        RoleWorkspaceContent content = RoleWorkspaceContentFactory.create(user);
        workspaceTitleLabel.setText(content.title());
        workspaceSubtitleLabel.setText("Quyền hiện tại: " + user.roleName());
        workspaceSummaryLabel.setText(content.summary());
        accessNoteLabel.setText(content.accessNote());
        statusBadgeLabel.setText(content.statusLabel());

        capabilityListContainer.getChildren().clear();
        for (String capability : content.capabilityHighlights()) {
            capabilityListContainer.getChildren().add(buildCapabilityRow(capability));
        }
    }

    private HBox buildCapabilityRow(String text) {
        HBox row = new HBox(12);
        row.getStyleClass().add("role-capability-row");
        row.setPadding(new Insets(14, 16, 14, 16));

        Region dot = new Region();
        dot.getStyleClass().add("role-capability-dot");
        dot.setMinSize(10, 10);
        dot.setPrefSize(10, 10);
        dot.setMaxSize(10, 10);

        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("role-capability-text");
        HBox.setHgrow(label, Priority.ALWAYS);

        row.getChildren().addAll(dot, label);
        return row;
    }
}
