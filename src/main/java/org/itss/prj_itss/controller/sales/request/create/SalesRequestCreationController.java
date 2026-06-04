package org.itss.prj_itss.controller.sales.request.create;

import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationApplicationService;
import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationItemDraft;
import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationResult;

import java.util.List;
import java.util.Objects;

public final class SalesRequestCreationController {

    private final SalesRequestCreationApplicationService creationService;

    public SalesRequestCreationController(SalesRequestCreationApplicationService creationService) {
        this.creationService = Objects.requireNonNull(creationService, "creationService");
    }

    public void start(ISalesRequestCreationViewPort screen, Runnable onCreated) {
        Objects.requireNonNull(screen, "screen");

        screen.bindEvents(items -> submit(screen, items, onCreated));
        screen.render(new SalesRequestCreationViewState(creationService.findMerchandiseOptions()));
    }

    private void submit(
            ISalesRequestCreationViewPort screen,
            List<SalesRequestCreationItemInput> items,
        Runnable onCreated
    ) {
        SalesRequestCreationResult result = creationService.createRequest(toDrafts(items), "");
        if (!result.success()) {
            screen.showError("Lỗi khi lưu yêu cầu: " + result.message());
            return;
        }

        screen.showSuccess(result.message());
        if (onCreated != null) {
            onCreated.run();
        }
        screen.close();
    }

    private List<SalesRequestCreationItemDraft> toDrafts(List<SalesRequestCreationItemInput> items) {
        return items.stream()
            .map(item -> new SalesRequestCreationItemDraft(
                item.merchandiseCode(),
                item.quantityText(),
                item.desiredDate()
            ))
            .toList();
    }
}
