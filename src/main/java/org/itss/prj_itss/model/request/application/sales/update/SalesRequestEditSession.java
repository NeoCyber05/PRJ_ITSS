package org.itss.prj_itss.model.request.application.sales.update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesRequestEditSession {

    SalesRequestEditLoadResult currentView();

    SalesRequestEditCommandResult addBlankItem();

    SalesRequestEditCommandResult removeItem(int lineId);

    SalesRequestEditCommandResult removeItems(List<Integer> lineIds);

    SalesRequestEditCommandResult changeMerchandise(int lineId, Integer merchandiseId);

    SalesRequestEditCommandResult changeQuantity(int lineId, BigDecimal quantity);

    SalesRequestEditCommandResult changeDesiredDate(int lineId, LocalDate desiredDate);

    SalesRequestEditSaveResult save();

    SalesRequestEditDraft snapshot();
}
