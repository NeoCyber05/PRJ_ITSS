package org.itss.prj_itss.ordering.request.process.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.itss.prj_itss.model.DeliveryMethod;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.OrderLineSuggestion;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.SiteOrderSuggestion;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.SuggestedPlan;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.applyMainStylesheet;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.buildColumnHeader;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.showToast;

public final class AllSuggestPopupView {

    private static final String POPUP_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/ui/all-suggest-popup-view.fxml";

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox plansBox;

    @FXML
    private Button closeButton;

    private List<SuggestedPlan> plans = List.of();
    private Consumer<SuggestedPlan> onApply = plan -> {};
    private Stage dialog;

    public static void show(List<SuggestedPlan> plans, Consumer<SuggestedPlan> onApply) {
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

    private void init(Stage dialog, List<SuggestedPlan> plans, Consumer<SuggestedPlan> onApply) {
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
            plansBox.getChildren().add(buildEmptyCard());
        } else {
            for (int index = 0; index < plans.size(); index++) {
                plansBox.getChildren().add(buildPlanCard(plans.get(index), index + 1, dialog));
            }
        }
    }

    private VBox buildEmptyCard() {
        VBox emptyCard = new VBox(8);
        emptyCard.setPadding(new Insets(18));
        addStyleClass(emptyCard, "allocation-table");

        Label emptyTitle = new Label("Chưa có phương án thỏa mãn");
        addStyleClass(emptyTitle, "allocation-card-title");

        Label emptyText = new Label("Kiểm tra lại site bị loại bỏ, số lượng tồn kho hoặc ngày giao yêu cầu.");
        emptyText.setWrapText(true);
        addStyleClass(emptyText, "allocation-subtitle");

        emptyCard.getChildren().addAll(emptyTitle, emptyText);
        return emptyCard;
    }

    private VBox buildPlanCard(SuggestedPlan plan, int number, Stage dialog) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(16));
        addStyleClass(card, "allocation-plan-card");

        card.getChildren().add(buildPlanHeader(plan, number, dialog));

        VBox siteOrdersBox = new VBox(12);
        for (SiteOrderSuggestion siteOrder : plan.siteOrders()) {
            siteOrdersBox.getChildren().add(buildSiteOrderCard(siteOrder));
        }

        card.getChildren().add(siteOrdersBox);
        return card;
    }

    private HBox buildPlanHeader(SuggestedPlan plan, int number, Stage dialog) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(6);
        Label titleLabel = new Label("Phương án " + String.format("%02d", number));
        addStyleClass(titleLabel, "allocation-title");

        Label summaryLabel = new Label(
            plan.siteOrders().size() + " site"
                + " • " + plan.totalLineCount() + " dòng đặt hàng"
                + " • " + plan.totalQuantity() + " chiếc"
        );
        addStyleClass(summaryLabel, "allocation-subtitle");
        titleBox.getChildren().addAll(titleLabel, summaryLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tagRow = new HBox(8);
        tagRow.setAlignment(Pos.CENTER_RIGHT);
        tagRow.getChildren().addAll(
            buildMetricTag("Đủ số lượng", "allocation-metric-success"),
            buildMetricTag("Kịp ngày nhận", "allocation-metric-info")
        );

        Button applyButton = new Button("Áp dụng phương án này");
        addStyleClass(applyButton, "allocation-apply-plan-button");
        applyButton.setOnAction(event -> {
            onApply.accept(plan);
            dialog.close();
            showToast("Đã áp dụng phương án " + number + ".");
        });

        VBox headerRight = new VBox(10, tagRow, applyButton);
        headerRight.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().addAll(titleBox, spacer, headerRight);
        return header;
    }

    private VBox buildSiteOrderCard(SiteOrderSuggestion siteOrder) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        addStyleClass(card, "allocation-site-order-card");

        card.getChildren().add(buildSiteOrderHeader(siteOrder));

        VBox table = new VBox(0);
        addStyleClass(table, "allocation-suggested-table");
        table.getChildren().add(buildTableHeader());
        for (OrderLineSuggestion line : siteOrder.lines()) {
            table.getChildren().add(buildTableRow(line));
        }

        card.getChildren().add(table);
        return card;
    }

    private HBox buildSiteOrderHeader(SiteOrderSuggestion siteOrder) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox siteBox = new VBox(4);
        Label siteNameLabel = new Label(siteOrder.site().name);
        addStyleClass(siteNameLabel, "allocation-card-title");

        Label siteMetaLabel = new Label(
            siteOrder.site().siteCode
                + " • " + siteOrder.lines().size() + " mặt hàng"
                + " • " + siteOrder.totalQuantity() + " chiếc"
        );
        addStyleClass(siteMetaLabel, "allocation-subtitle");
        siteBox.getChildren().addAll(siteNameLabel, siteMetaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tagRow = new HBox(8);
        tagRow.setAlignment(Pos.CENTER_RIGHT);
        tagRow.getChildren().addAll(
            buildMetricTag(siteOrder.transportSummary(), "allocation-metric-info"),
            buildMetricTag("ETA " + siteOrder.deliveryDays() + " ngày", "allocation-metric-success")
        );

        header.getChildren().addAll(siteBox, spacer, tagRow);
        return header;
    }

    private HBox buildTableHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        addStyleClass(header, "allocation-suggested-table-header");
        header.getChildren().addAll(
            buildColumnHeader("MÃ HÀNG", 130),
            buildColumnHeader("TÊN MẶT HÀNG", 280),
            buildColumnHeader("SỐ LƯỢNG", 120),
            buildColumnHeader("VẬN CHUYỂN", 150)
        );
        return header;
    }

    private HBox buildTableRow(OrderLineSuggestion line) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        addStyleClass(row, "allocation-table-row");

        row.getChildren().addAll(
            buildValueCell(line.item().code, 130, true),
            buildValueCell(line.item().name, 280, false),
            buildValueCell(line.quantity() + " chiếc", 120, true),
            buildValueCell(DeliveryMethod.displayLabelOf(line.transport()), 150, false)
        );
        return row;
    }

    private Label buildMetricTag(String text, String modifierClass) {
        Label label = new Label(text);
        addStyleClass(label, "allocation-metric-tag", modifierClass);
        return label;
    }

    private Label buildValueCell(String text, double width, boolean emphasize) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setWrapText(true);
        addStyleClass(label, "allocation-value-cell");
        if (emphasize) {
            addStyleClass(label, "allocation-value-cell-emphasis");
        }
        return label;
    }
}
