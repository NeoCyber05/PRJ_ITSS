package org.itss.prj_itss.common.presentation.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.Function;

public final class TableViewSupport {

    private TableViewSupport() {
    }

    public static <T> void useConstrainedResize(TableView<T> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public static <T> void bindStringColumn(TableColumn<T, String> column, Function<T, String> valueProvider) {
        column.setCellValueFactory(data -> new SimpleStringProperty(valueProvider.apply(data.getValue())));
    }

    public static <T> void bindRowColumn(TableColumn<T, T> column) {
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    }

    public static <T> void bindHeightToRows(
        TableView<T> table,
        ObservableList<?> rows,
        double fixedCellSize,
        double headerHeight
    ) {
        table.setFixedCellSize(fixedCellSize);
        table.prefHeightProperty().bind(
            Bindings.max(1, Bindings.size(rows)).multiply(table.getFixedCellSize()).add(headerHeight)
        );
        table.minHeightProperty().bind(table.prefHeightProperty());
        table.maxHeightProperty().bind(table.prefHeightProperty());
    }

    public static <T> void setItems(TableView<T> table, ObservableList<T> rows) {
        table.setItems(rows);
    }

    public static void setEmptyPlaceholder(TableView<?> table, String message) {
        table.setPlaceholder(new Label(message));
    }

    public static void addStyleClass(Node node, String... styleClasses) {
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }
}
