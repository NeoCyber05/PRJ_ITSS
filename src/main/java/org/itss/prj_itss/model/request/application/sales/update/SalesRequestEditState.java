package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SalesRequestEditState {

    private final int requestId;
    private final LocalDateTime createdAt;
    private final String status;
    private final List<SalesRequestEditItemDraft> items = new ArrayList<>();
    private int nextLineId = 1;

    public SalesRequestEditState(int requestId, LocalDateTime createdAt, String status) {
        this.requestId = requestId;
        this.createdAt = createdAt;
        this.status = status;
    }

    public void replaceItems(List<SalesRequestEditItemDraft> newItems) {
        items.clear();
        items.addAll(newItems);
        nextLineId = items.stream()
            .mapToInt(SalesRequestEditItemDraft::lineId)
            .max()
            .orElse(0) + 1;
    }

    public SalesRequestEditDraft snapshot() {
        return new SalesRequestEditDraft(requestId, createdAt, status, items);
    }

    public void addBlankItem() {
        items.add(new SalesRequestEditItemDraft(nextLineId++, null, null, null));
    }

    public void removeItem(int lineId) {
        items.removeIf(item -> item.lineId() == lineId);
    }

    public void removeItems(Set<Integer> lineIds) {
        items.removeIf(item -> lineIds.contains(item.lineId()));
    }

    public void changeMerchandise(int lineId, MerchandiseOption merchandise) {
        replace(lineId, item -> new SalesRequestEditItemDraft(
            item.lineId(),
            merchandise,
            item.quantity(),
            item.desiredDate()
        ));
    }

    public void changeQuantity(int lineId, BigDecimal quantity) {
        replace(lineId, item -> new SalesRequestEditItemDraft(
            item.lineId(),
            item.merchandise(),
            quantity,
            item.desiredDate()
        ));
    }

    public void changeDesiredDate(int lineId, LocalDate desiredDate) {
        replace(lineId, item -> new SalesRequestEditItemDraft(
            item.lineId(),
            item.merchandise(),
            item.quantity(),
            desiredDate
        ));
    }

    private void replace(int lineId, IItemReplacement replacement) {
        for (int index = 0; index < items.size(); index++) {
            SalesRequestEditItemDraft item = items.get(index);
            if (item.lineId() == lineId) {
                items.set(index, replacement.replace(item));
                return;
            }
        }
    }

    private interface IItemReplacement {
        SalesRequestEditItemDraft replace(SalesRequestEditItemDraft item);
    }
}
