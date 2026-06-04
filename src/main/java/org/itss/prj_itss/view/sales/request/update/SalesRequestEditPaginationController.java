package org.itss.prj_itss.view.sales.request.update;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.itss.prj_itss.view.sales.request.shared.ItemRow;

final class SalesRequestEditPaginationController {

    private final int pageSize;
    private final FilteredList<ItemRow> filteredItems;
    private final ObservableList<ItemRow> pageItems;
    private final Label itemCountLabel;
    private final HBox paginationBox;
    private int currentPage;

    SalesRequestEditPaginationController(
            int pageSize,
            FilteredList<ItemRow> filteredItems,
            ObservableList<ItemRow> pageItems,
            Label itemCountLabel,
            HBox paginationBox
    ) {
        this.pageSize = pageSize;
        this.filteredItems = filteredItems;
        this.pageItems = pageItems;
        this.itemCountLabel = itemCountLabel;
        this.paginationBox = paginationBox;
    }

    void updatePageView() {
        int total = filteredItems.size();
        int totalPages = pageCount();
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int from = currentPage * pageSize;
        int to = Math.min(from + pageSize, total);
        pageItems.setAll(filteredItems.subList(from, to));

        itemCountLabel.setText(total == 0
            ? "Không có mặt hàng"
            : "Hiển thị " + (from + 1) + " - " + to + " / " + total + " mặt hàng");

        buildPaginationButtons(totalPages);
    }

    void showFilteredIndex(int filteredIndex) {
        currentPage = filteredIndex / pageSize;
        updatePageView();
    }

    int pageIndex(int filteredIndex) {
        return filteredIndex % pageSize;
    }

    private int pageCount() {
        return Math.max(1, (int) Math.ceil((double) filteredItems.size() / pageSize));
    }

    private void buildPaginationButtons(int totalPages) {
        paginationBox.getChildren().clear();
        if (totalPages <= 1) {
            return;
        }

        Button prev = pageButton("<");
        prev.setDisable(currentPage <= 0);
        prev.setOnAction(event -> {
            currentPage--;
            updatePageView();
        });
        paginationBox.getChildren().add(prev);

        for (int i = 0; i < totalPages; i++) {
            Button button = pageButton(String.valueOf(i + 1));
            button.setStyle(pageButtonStyle(i == currentPage));
            int page = i;
            button.setOnAction(event -> {
                currentPage = page;
                updatePageView();
            });
            paginationBox.getChildren().add(button);
        }

        Button next = pageButton(">");
        next.setDisable(currentPage >= totalPages - 1);
        next.setOnAction(event -> {
            currentPage++;
            updatePageView();
        });
        paginationBox.getChildren().add(next);
    }

    private Button pageButton(String text) {
        Button button = new Button(text);
        button.setStyle(pageButtonStyle(false));
        return button;
    }

    private String pageButtonStyle(boolean active) {
        return active
            ? "-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;"
            : "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;";
    }
}
