package org.itss.prj_itss.controller.sales.merchandise;

import org.itss.prj_itss.model.merchandise.application.MerchandiseDraft;
import org.itss.prj_itss.model.merchandise.application.MerchandiseManagementResult;
import org.itss.prj_itss.model.merchandise.application.MerchandiseManagementService;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;

import java.util.List;
import java.util.Locale;

public final class SalesMerchandiseController {

    private final MerchandiseManagementService merchandiseManagementService;

    public SalesMerchandiseController(MerchandiseManagementService merchandiseManagementService) {
        this.merchandiseManagementService = merchandiseManagementService;
    }

    public List<MerchandiseRow> loadAll() {
        return merchandiseManagementService.findAll().stream()
            .map(this::toRow)
            .toList();
    }

    public List<MerchandiseRow> filterRows(List<MerchandiseRow> rows, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return rows;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return rows.stream()
            .filter(row ->
                row.code().toLowerCase(Locale.ROOT).contains(normalized) ||
                row.name().toLowerCase(Locale.ROOT).contains(normalized) ||
                row.unit().toLowerCase(Locale.ROOT).contains(normalized))
            .toList();
    }

    public MerchandiseManagementResult create(MerchandiseDraft draft) {
        return merchandiseManagementService.create(draft);
    }

    public MerchandiseManagementResult update(int id, MerchandiseDraft draft) {
        return merchandiseManagementService.update(id, draft);
    }

    public MerchandiseManagementResult deactivate(int id) {
        return merchandiseManagementService.deactivate(id);
    }

    public MerchandiseManagementResult restore(int id) {
        return merchandiseManagementService.restore(id);
    }

    private MerchandiseRow toRow(Merchandise merchandise) {
        return new MerchandiseRow(
            merchandise.getId(),
            merchandise.getCode(),
            merchandise.getName(),
            merchandise.getUnit(),
            merchandise.isActive()
        );
    }

    public record MerchandiseRow(
        int id,
        String code,
        String name,
        String unit,
        boolean active
    ) {
    }
}
