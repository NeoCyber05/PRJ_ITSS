package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.request.application.sales.SalesRequestPort;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;
import org.itss.prj_itss.model.request.application.sales.view.RequestReadOnlyView;

import java.math.BigDecimal;
import java.util.List;

public final class RequestSalesApplicationService {

    private final SalesRequestPort requestService;
    private final CatalogUseCase catalogUseCase;

    public RequestSalesApplicationService(SalesRequestPort requestService, CatalogUseCase catalogUseCase) {
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
            request.getStatusKey(),
            OrderingFormatters.requestStatusText(request.getStatusKey()),
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
                    item.getQuantityOrdered() != null ? OrderingFormatters.formatQuantity(item.getQuantityOrdered()) : "0",
                    OrderingFormatters.formatDate(item.getDesiredDeliveryDate())
                );
            })
            .toList();

        return new RequestFormView(
            request.getId(),
            OrderingFormatters.formatRequestCode(request.getId()),
            OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
            request.getStatusKey(),
            OrderingFormatters.requestStatusText(request.getStatusKey()),
            request.getNote(),
            itemRows
        );
    }

    public int createRequest(List<SalesRequestItemSubmission> items, String note) throws Exception {
        List<RequestMerchandise> domainItems = items.stream()
            .map(i -> new RequestMerchandise(0, i.merchandiseId(), i.quantityOrdered(), i.desiredDeliveryDate()))
            .toList();
        return requestService.createRequest(domainItems, note);
    }

    public void updateRequest(int requestId, List<SalesRequestItemSubmission> items, String note) throws Exception {
        List<RequestMerchandise> domainItems = items.stream()
            .map(i -> new RequestMerchandise(requestId, i.merchandiseId(), i.quantityOrdered(), i.desiredDeliveryDate()))
            .toList();
        requestService.updateRequestItems(requestId, domainItems, note);
    }

    public boolean deleteRequest(int requestId) {
        return requestService.deleteById(requestId);
    }

    private RequestDetailItemRow toDetailRow(RequestMerchandise item) {
        MerchandiseOption m = findMerchandiseOptionById(item.getMerchandiseId());
        return new RequestDetailItemRow(
            m != null ? m.code() : "N/A",
            m != null ? m.name() : "N/A",
            item.getQuantityOrdered() != null ? OrderingFormatters.formatQuantity(item.getQuantityOrdered()) : "0",
            m != null ? m.unit() : "N/A",
            OrderingFormatters.formatDate(item.getDesiredDeliveryDate())
        );
    }
}
