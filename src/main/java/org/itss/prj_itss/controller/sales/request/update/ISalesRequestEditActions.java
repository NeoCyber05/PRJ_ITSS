package org.itss.prj_itss.controller.sales.request.update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ISalesRequestEditActions {

    void addItemRequested();

    void deleteItemRequested(int lineId);

    void deleteItemsRequested(List<Integer> lineIds);

    void merchandiseChanged(int lineId, Integer merchandiseId);

    void quantityChanged(int lineId, BigDecimal quantity);

    void desiredDateChanged(int lineId, LocalDate desiredDate);

    void saveRequested();

    void cancelRequested();
}
