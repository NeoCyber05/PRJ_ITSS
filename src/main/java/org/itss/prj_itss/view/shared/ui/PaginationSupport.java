package org.itss.prj_itss.view.shared.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Reusable pagination logic for any list-based view.
 * Manages page state (currentPage, pageSize) and slices a source list
 * into a paginated ObservableList that can be bound to a TableView.
 */
public final class PaginationSupport<T> {

    private final ObservableList<T> paginatedItems = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int pageSize;

    public PaginationSupport(int defaultPageSize) {
        this.pageSize = defaultPageSize;
    }

    public ObservableList<T> paginatedItems() {
        return paginatedItems;
    }

    public void resetPage() {
        currentPage = 0;
    }

    /**
     * Recalculates the paginated slice from the given source list
     * and updates the provided UI controls.
     */
    public void update(
            List<T> sourceItems,
            Label infoLabel,
            Label pageIndicatorLabel,
            Button prevButton,
            Button nextButton,
            String emptyMessage,
            String itemLabel) {
        int totalItems = sourceItems.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        if (fromIndex <= totalItems && fromIndex < toIndex) {
            paginatedItems.setAll(sourceItems.subList(fromIndex, toIndex));
        } else {
            paginatedItems.clear();
        }

        int displayFrom = totalItems == 0 ? 0 : fromIndex + 1;
        infoLabel.setText(totalItems == 0
            ? emptyMessage
            : String.format("Hiển thị %d - %d của %d %s", displayFrom, toIndex, totalItems, itemLabel));

        pageIndicatorLabel.setText(String.format("Trang %d / %d", currentPage + 1, totalPages));

        prevButton.setDisable(currentPage <= 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
    }

    public void goToPrevPage(List<T> sourceItems, Label infoLabel, Label pageIndicatorLabel, Button prevButton, Button nextButton, String emptyMessage, String itemLabel) {
        if (currentPage > 0) {
            currentPage--;
            update(sourceItems, infoLabel, pageIndicatorLabel, prevButton, nextButton, emptyMessage, itemLabel);
        }
    }

    public void goToNextPage(List<T> sourceItems, Label infoLabel, Label pageIndicatorLabel, Button prevButton, Button nextButton, String emptyMessage, String itemLabel) {
        int totalItems = sourceItems.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage < totalPages - 1) {
            currentPage++;
            update(sourceItems, infoLabel, pageIndicatorLabel, prevButton, nextButton, emptyMessage, itemLabel);
        }
    }

    /**
     * Binds a page-size ComboBox so that changing its value
     * resets to page 0 and re-paginates.
     */
    public void bindPageSizeComboBox(
            ComboBox<Integer> comboBox,
            List<T> sourceItems,
            Label infoLabel,
            Label pageIndicatorLabel,
            Button prevButton,
            Button nextButton,
            String emptyMessage,
            String itemLabel) {
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 0;
                update(sourceItems, infoLabel, pageIndicatorLabel, prevButton, nextButton, emptyMessage, itemLabel);
            }
        });
    }
}
