package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SalesRequestEditSelectionPolicy {

    public List<MerchandiseOption> availableOptions(
            int currentLineId,
            List<SalesRequestEditItemDraft> items,
            List<MerchandiseOption> allOptions
    ) {
        Set<Integer> usedMerchandiseIds = selectedMerchandiseIdsExcept(currentLineId, items);
        return allOptions.stream()
            .filter(option -> !usedMerchandiseIds.contains(option.id()))
            .toList();
    }

    public boolean hasDuplicateMerchandise(SalesRequestEditDraft draft) {
        Set<Integer> seenMerchandiseIds = new HashSet<>();
        for (SalesRequestEditItemDraft item : draft.items()) {
            MerchandiseOption merchandise = item.merchandise();
            if (merchandise != null && !seenMerchandiseIds.add(merchandise.id())) {
                return true;
            }
        }
        return false;
    }

    public boolean isDuplicateSelection(
            int currentLineId,
            int merchandiseId,
            List<SalesRequestEditItemDraft> items
    ) {
        for (SalesRequestEditItemDraft item : items) {
            if (item.lineId() == currentLineId || item.merchandise() == null) {
                continue;
            }
            if (item.merchandise().id() == merchandiseId) {
                return true;
            }
        }
        return false;
    }

    private Set<Integer> selectedMerchandiseIdsExcept(int currentLineId, List<SalesRequestEditItemDraft> items) {
        Set<Integer> selectedMerchandiseIds = new HashSet<>();
        for (SalesRequestEditItemDraft item : items) {
            if (item.lineId() != currentLineId && item.merchandise() != null) {
                selectedMerchandiseIds.add(item.merchandise().id());
            }
        }
        return selectedMerchandiseIds;
    }
}
