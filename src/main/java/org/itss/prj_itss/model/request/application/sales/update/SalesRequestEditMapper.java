package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SalesRequestEditMapper {

    public SalesRequestEditState toState(
            Request request,
            List<RequestMerchandise> requestItems,
            List<MerchandiseOption> merchandiseOptions
    ) {
        SalesRequestEditState state = new SalesRequestEditState(
            request.getId(),
            request.getCreatedAt(),
            request.getStatusKey()
        );

        Map<Integer, MerchandiseOption> merchandiseById = merchandiseOptions.stream()
            .collect(Collectors.toMap(MerchandiseOption::id, Function.identity()));
        List<SalesRequestEditItemDraft> items = new ArrayList<>();
        int lineId = 1;
        for (RequestMerchandise item : requestItems) {
            MerchandiseOption merchandise = merchandiseById.get(item.getMerchandiseId());
            if (merchandise == null) {
                continue;
            }
            items.add(new SalesRequestEditItemDraft(
                lineId++,
                merchandise,
                item.getQuantityOrdered(),
                item.getDesiredDeliveryDate()
            ));
        }
        state.replaceItems(items);
        return state;
    }

    public List<SalesRequestItemSubmission> toInput(SalesRequestEditDraft draft) {
        return draft.items().stream()
            .map(item -> new SalesRequestItemSubmission(
                item.merchandise().id(),
                item.quantity(),
                item.desiredDate()
            ))
            .toList();
    }
}
