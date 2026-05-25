package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.math.BigDecimal;
import java.util.List;

public final class RequestSalesApplicationService {

    private final RequestManagementUseCase requestService;
    private final CatalogUseCase catalogUseCase;

    public RequestSalesApplicationService(RequestManagementUseCase requestService, CatalogUseCase catalogUseCase) {
        this.requestService = requestService;
        this.catalogUseCase = catalogUseCase;
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return catalogUseCase.findAll().stream()
            .map(m -> new MerchandiseOption(m.getId(), m.getCode(), m.getName(), m.getUnit()))
            .toList();
    }

    public MerchandiseOption findMerchandiseOptionByCode(String code) {
        Merchandise m = catalogUseCase.findByCode(code);
        if (m == null) return null;
        return new MerchandiseOption(m.getId(), m.getCode(), m.getName(), m.getUnit());
    }

    public MerchandiseOption findMerchandiseOptionById(int id) {
        Merchandise m = catalogUseCase.findById(id);
        if (m == null) return null;
        return new MerchandiseOption(m.getId(), m.getCode(), m.getName(), m.getUnit());
    }

    public RequestReadOnlyView findReadOnlyView(int requestId) {
        Request request = requestService.findById(requestId);
        if (request == null) return null;

        List<RequestDetailItemRow> itemRows = requestService.findItemsByRequestId(requestId).stream()
            .map(this::toDetailRow)
            .toList();

        return new RequestReadOnlyView(
            request.getId(),
            OrderingFormatters.formatRequestCode(request.getId()),
            OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
            request.getStatus(),
            OrderingFormatters.requestStatusText(request.getStatus()),
            request.getNote(),
            itemRows
        );
    }

    public RequestFormView findFormView(int requestId) {
        Request request = requestService.findById(requestId);
        if (request == null) return null;

        List<RequestFormView.RequestItemFormRow> itemRows = requestService.findItemsByRequestId(requestId).stream()
            .map(item -> {
                MerchandiseOption m = findMerchandiseOptionById(item.getMerchandiseId());
                return new RequestFormView.RequestItemFormRow(
                    m,
                    item.getQuantityOrdered() != null ? item.getQuantityOrdered().toPlainString() : "0",
                    OrderingFormatters.formatDate(item.getDesiredDeliveryDate())
                );
            })
            .toList();

        return new RequestFormView(
            request.getId(),
            OrderingFormatters.formatRequestCode(request.getId()),
            OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
            request.getStatus(),
            OrderingFormatters.requestStatusText(request.getStatus()),
            request.getNote(),
            itemRows
        );
    }

    public int createRequest(List<RequestItemInput> items, String note) throws Exception {
        List<RequestMerchandise> domainItems = items.stream()
            .map(i -> new RequestMerchandise(0, i.merchandiseId(), i.quantityOrdered(), i.desiredDeliveryDate()))
            .toList();
        return requestService.createRequest(domainItems, note);
    }

    public void updateRequest(int requestId, List<RequestItemInput> items, String note) throws Exception {
        List<RequestMerchandise> domainItems = items.stream()
            .map(i -> new RequestMerchandise(requestId, i.merchandiseId(), i.quantityOrdered(), i.desiredDeliveryDate()))
            .toList();
        requestService.updateRequestItems(requestId, domainItems, note);
    }

    public boolean deleteRequest(int requestId) {
        return requestService.deleteRequest(requestId);
    }

    private RequestDetailItemRow toDetailRow(RequestMerchandise item) {
        MerchandiseOption m = findMerchandiseOptionById(item.getMerchandiseId());
        return new RequestDetailItemRow(
            m != null ? m.code() : "N/A",
            m != null ? m.name() : "N/A",
            item.getQuantityOrdered() != null ? item.getQuantityOrdered().toPlainString() : "0",
            m != null ? m.unit() : "N/A",
            OrderingFormatters.formatDate(item.getDesiredDeliveryDate())
        );
    }
}
