package org.itss.prj_itss.view.ordering.request.process.suggest;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.request.application.processing.SuggestedPlanView;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class SuggestPlanCardView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/suggest/suggest-plan-card.fxml";

    @FXML
    private Label planTitleLabel;
    @FXML
    private Label planSummaryLabel;
    @FXML
    private Label summaryText;
    @FXML
    private Button applyButton;

    private SuggestedPlanView plan;
    private int number;
    private Consumer<SuggestedPlanView> onApply = ignored -> {};

    public static VBox load(SuggestedPlanView plan, int number, Consumer<SuggestedPlanView> onApply) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SuggestPlanCardView.class.getResource(VIEW_RESOURCE),
                "Missing suggest plan card FXML"
            ));
            VBox root = loader.load();
            SuggestPlanCardView controller = loader.getController();
            controller.init(plan, number, onApply);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load suggest plan card view", exception);
        }
    }

    @FXML
    private void handleApply() {
        onApply.accept(plan);
    }

    private void init(SuggestedPlanView plan, int number, Consumer<SuggestedPlanView> onApply) {
        this.plan = Objects.requireNonNull(plan, "plan");
        this.number = number;
        this.onApply = onApply == null ? ignored -> {} : onApply;

        planTitleLabel.setText("Phương án " + String.format("%02d", number));
        planSummaryLabel.setText(
            plan.siteCount() + " site"
                + " • " + plan.totalLineCount() + " dòng đặt hàng"
                + " • " + plan.totalQuantity() + " chiếc"
        );
        summaryText.setText(
            "Tổng: " + plan.totalQuantity() + " chiếc, "
                + plan.totalLineCount() + " dòng, "
                + plan.siteCount() + " site"
        );
    }
}
