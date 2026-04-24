package org.itss.prj_itss.auth.workspace;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.auth.session.SessionAwareController;
import org.itss.prj_itss.auth.session.UserSession;
import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.layout.ViewController;

public class RoleWorkspaceController implements ViewController, SessionAwareController {

    private UserSession userSession;

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

    @Override
    public void init(Navigator navigator, ApplicationContext context) {
        refresh();
    }

    @Override
    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
        refresh();
    }

    private void refresh() {
        if (userSession == null || workspaceTitleLabel == null) {
            return;
        }

        RoleWorkspaceContent content = RoleWorkspaceContentFactory.create(userSession);
        workspaceTitleLabel.setText(content.title());
        workspaceSubtitleLabel.setText("Quyền hiện tại: " + userSession.roleName());
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
