package org.itss.prj_itss.view.ordering.request.process.suggest;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.itss.prj_itss.model.request.application.processing.SuggestedPlanView;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.applyMainStylesheet;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.showToast;

public final class AllSuggestPopupView {

    private static final String POPUP_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/suggest/all-suggest-popup-view.fxml";
    private static final String EMPTY_CARD_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/suggest/suggest-empty-card.fxml";

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox plansBox;

    @FXML
    private Button closeButton;

    private List<SuggestedPlanView> plans = List.of();
    private Consumer<SuggestedPlanView> onApply = plan -> {};
    private Stage dialog;

    public static void show(List<SuggestedPlanView> plans, Consumer<SuggestedPlanView> onApply) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Các phương án phân bổ thỏa mãn");
        dialog.setResizable(true);

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllSuggestPopupView.class.getResource(POPUP_RESOURCE),
                "Missing all suggest popup FXML"
            ));
            Parent root = loader.load();
            AllSuggestPopupView controller = loader.getController();
            controller.init(dialog, plans, onApply);

            Scene scene = new Scene(root);
            applyMainStylesheet(scene);
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load all suggest popup view", exception);
        }
    }

    private void init(Stage dialog, List<SuggestedPlanView> plans, Consumer<SuggestedPlanView> onApply) {
        this.dialog = dialog;
        this.plans = plans == null ? List.of() : plans;
        this.onApply = onApply == null ? plan -> {} : onApply;

        titleLabel.setText("Các phương án phân bổ thỏa mãn");
        subtitleLabel.setText(buildSubtitle());
        closeButton.setOnAction(event -> dialog.close());
        renderPlans();
    }

    private String buildSubtitle() {
        return plans.isEmpty()
            ? "Không tìm được phương án đáp ứng đủ số lượng và thời hạn hiện tại."
            : "Hiển thị " + plans.size() + " phương án khác nhau theo từng đơn hàng gửi tới site.";
    }

    private void renderPlans() {
        plansBox.getChildren().clear();
        if (plans.isEmpty()) {
            plansBox.getChildren().add(loadEmptyCard());
            return;
        }
        for (int index = 0; index < plans.size(); index++) {
            int number = index + 1;
            plansBox.getChildren().add(SuggestPlanCardView.load(plans.get(index), number, plan -> applyPlan(plan, number)));
        }
    }

    private void applyPlan(SuggestedPlanView plan, int number) {
        onApply.accept(plan);
        dialog.close();
        showToast("Đã áp dụng phương án " + number + ".");
    }

    private VBox loadEmptyCard() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllSuggestPopupView.class.getResource(EMPTY_CARD_RESOURCE),
                "Missing suggest empty card FXML"
            ));
            return loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load suggest empty card", exception);
        }
    }
}
