package org.itss.prj_itss.view.ordering.request.process.suggest;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.controller.ordering.request.process.state.SuggestedPlanState;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class SuggestPlanCardView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/suggest/suggest-plan-card-view.fxml";

    @FXML
    private Label planTitleLabel;
    @FXML
    private Label planSummaryLabel;
    @FXML
    private VBox siteDetailsBox;
    @FXML
    private Button applyButton;

    private SuggestedPlanState plan;
    private int number;
    private Consumer<SuggestedPlanState> onApply = ignored -> {};

    public static VBox load(SuggestedPlanState plan, int number, Consumer<SuggestedPlanState> onApply) {
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

    private void init(SuggestedPlanState plan, int number, Consumer<SuggestedPlanState> onApply) {
        this.plan = Objects.requireNonNull(plan, "plan");
        this.number = number;
        this.onApply = onApply == null ? ignored -> {} : onApply;

        planTitleLabel.setText("Phương án " + String.format("%02d", number));
        planSummaryLabel.setText(plan.siteCount() + " site");
        renderSiteDetails();
    }

    private void renderSiteDetails() {
        siteDetailsBox.getChildren().clear();
        boolean hasDetails = !plan.siteAllocations().isEmpty();
        siteDetailsBox.setManaged(hasDetails);
        siteDetailsBox.setVisible(hasDetails);
        if (!hasDetails) {
            return;
        }

        for (SuggestedPlanState.SuggestedSiteState site : plan.siteAllocations()) {
            siteDetailsBox.getChildren().add(buildSiteCard(site));
        }
    }

    private VBox buildSiteCard(SuggestedPlanState.SuggestedSiteState site) {
        VBox siteBox = new VBox(8);
        siteBox.getStyleClass().add("allocation-site-order-card");

        // ── Header: tên site + badges
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label siteNameLabel = new Label(formatSiteName(site));
        siteNameLabel.getStyleClass().add("allocation-plan-site-header");
        siteNameLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label qtyBadge = new Label(site.totalQuantity() + " chiếc");
        qtyBadge.getStyleClass().addAll("suggest-line-qty-badge");

        Label daysBadge = new Label(formatDays(site.deliveryDays()));
        daysBadge.getStyleClass().addAll("suggest-line-days-badge");

        headerRow.getChildren().addAll(siteNameLabel, spacer, qtyBadge, daysBadge);
        siteBox.getChildren().add(headerRow);

        // ── Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-background-color: #E8EEE9;");
        VBox.setMargin(divider, new Insets(0, 0, 0, 0));
        siteBox.getChildren().add(divider);

        // ── Danh sách dòng sản phẩm (hiển thị HẾT, không cắt)
        for (SuggestedPlanState.SuggestedLineState line : site.lines()) {
            siteBox.getChildren().add(buildLineRow(line));
        }

        return siteBox;
    }

    private HBox buildLineRow(SuggestedPlanState.SuggestedLineState line) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("suggest-line-row");

        // Tên sản phẩm (chiếm phần lớn không gian)
        VBox nameBox = new VBox(1);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        if (!line.itemCode().isBlank()) {
            Label codeLabel = new Label(line.itemCode());
            codeLabel.getStyleClass().add("suggest-line-code");
            nameBox.getChildren().add(codeLabel);
        }

        if (!line.itemName().isBlank()) {
            Label nameLabel = new Label(line.itemName());
            nameLabel.getStyleClass().add("suggest-line-name");
            nameLabel.setWrapText(true);
            nameBox.getChildren().add(nameLabel);
        } else if (line.itemCode().isBlank()) {
            Label fallback = new Label("Mặt hàng chưa đặt tên");
            fallback.getStyleClass().add("suggest-line-name");
            nameBox.getChildren().add(fallback);
        }

        // Số lượng badge
        Label qtyLabel = new Label(line.quantity() + " chiếc");
        qtyLabel.getStyleClass().add("suggest-line-qty-badge");

        // Vận chuyển badge
        Label transportLabel = new Label(line.transportLabel());
        transportLabel.getStyleClass().add("suggest-line-transport-badge");

        row.getChildren().addAll(nameBox, qtyLabel, transportLabel);
        return row;
    }

    private String formatSiteName(SuggestedPlanState.SuggestedSiteState site) {
        if (!site.siteName().isBlank() && !site.siteCode().isBlank()) {
            return site.siteName() + " (" + site.siteCode() + ")";
        }
        if (!site.siteName().isBlank()) {
            return site.siteName();
        }
        return site.siteCode().isBlank() ? "Site chưa đặt tên" : site.siteCode();
    }

    private String formatDays(int days) {
        return days <= 0 ? "Chưa rõ ngày" : days + " ngày";
    }
}
