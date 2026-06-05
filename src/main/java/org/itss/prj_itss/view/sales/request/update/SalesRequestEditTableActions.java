package org.itss.prj_itss.view.sales.request.update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

record SalesRequestEditTableActions(
        Runnable addItem,
        IntConsumer deleteItem,
        Consumer<List<Integer>> deleteItems,
        BiConsumer<Integer, Integer> merchandiseChanged,
        BiConsumer<Integer, BigDecimal> quantityChanged,
        BiConsumer<Integer, LocalDate> desiredDateChanged
) {
}
