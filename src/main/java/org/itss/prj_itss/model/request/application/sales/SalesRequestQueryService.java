package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;
import org.itss.prj_itss.model.request.application.sales.view.RequestReadOnlyView;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;

import java.util.List;
import java.util.Map;

public class SalesRequestQueryService {

    private final SalesRequestQueryPort queryPort;
    private final MerchandiseUseCase MerchandiseUseCase;
    private final InventoryRepository inventoryRepository;

    public SalesRequestQueryService(SalesRequestQueryPort queryPort, MerchandiseUseCase MerchandiseUseCase,
            InventoryRepository inventoryRepository) {
        this.queryPort = queryPort;
        this.MerchandiseUseCase = MerchandiseUseCase;
        this.inventoryRepository = inventoryRepository;
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return MerchandiseUseCase.findActive().stream()
                .map(this::toOption)
                .toList();
    }

    public MerchandiseOption findMerchandiseOptionByCode(String code) {
        Merchandise m = MerchandiseUseCase.findByCode(code);
        if (m == null)
            return null;
        return toOption(m);
    }

    public int getAvailableStock(String code) {
        Merchandise m = MerchandiseUseCase.findByCode(code);
        if (m == null)
            return 0;
        return inventoryRepository.getTotalStock(m.getId());
    }

    public MerchandiseOption findMerchandiseOptionById(int id) {
        Merchandise m = MerchandiseUseCase.findById(id);
        if (m == null)
            return null;
        return toOption(m);
    }

    public RequestReadOnlyView findReadOnlyView(int requestId) {
        Request request = queryPort.findById(requestId);
        if (request == null)
            return null;

        List<RequestMerchandise> requestItems = queryPort.findItemsByRequestId(requestId);
        Map<Integer, MerchandiseOption> merchandiseOptionsById = findMerchandiseOptionsByItemIds(requestItems);
        List<RequestDetailItemRow> itemRows = requestItems.stream()
                .map(item -> toDetailRow(item, merchandiseOptionsById.get(item.getMerchandiseId())))
                .toList();

        return new RequestReadOnlyView(
                request.getId(),
                OrderingFormatters.formatRequestCode(request.getId()),
                OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
                request.getStatusKey(),
                OrderingFormatters.requestStatusText(request.getStatusKey()),
                request.getNote(),
                itemRows);
    }

    public RequestFormView findFormView(int requestId) {
        Request request = queryPort.findById(requestId);
        if (request == null)
            return null;

        List<RequestMerchandise> requestItems = queryPort.findItemsByRequestId(requestId);
        Map<Integer, MerchandiseOption> merchandiseOptionsById = findMerchandiseOptionsByItemIds(requestItems);
        List<RequestFormView.RequestItemFormRow> itemRows = requestItems.stream()
                .map(item -> {
                    MerchandiseOption m = merchandiseOptionsById.get(item.getMerchandiseId());
                    return new RequestFormView.RequestItemFormRow(
                            m,
                            item.getQuantityOrdered() != null
                                    ? OrderingFormatters.formatQuantity(item.getQuantityOrdered())
                                    : "0",
                            OrderingFormatters.formatDate(item.getDesiredDeliveryDate()));
                })
                .toList();

        return new RequestFormView(
                request.getId(),
                OrderingFormatters.formatRequestCode(request.getId()),
                OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
                request.getStatusKey(),
                OrderingFormatters.requestStatusText(request.getStatusKey()),
                request.getNote(),
                itemRows);
    }

    private Map<Integer, MerchandiseOption> findMerchandiseOptionsByItemIds(List<RequestMerchandise> items) {
        List<Integer> merchandiseIds = items.stream()
            .map(RequestMerchandise::getMerchandiseId)
            .distinct()
            .toList();
        return MerchandiseUseCase.findByIds(merchandiseIds).values().stream()
            .collect(java.util.stream.Collectors.toMap(
                Merchandise::getId,
                this::toOption,
                (a, b) -> a,
                java.util.LinkedHashMap::new
            ));
    }

    private RequestDetailItemRow toDetailRow(RequestMerchandise item, MerchandiseOption m) {
        return new RequestDetailItemRow(
                m != null ? m.code() : "N/A",
                m != null ? m.name() : "N/A",
                item.getQuantityOrdered() != null ? OrderingFormatters.formatQuantity(item.getQuantityOrdered()) : "0",
                m != null ? m.unit() : "N/A",
                OrderingFormatters.formatDate(item.getDesiredDeliveryDate()));
    }

    private MerchandiseOption toOption(Merchandise merchandise) {
        return new MerchandiseOption(
            merchandise.getId(),
            merchandise.getCode(),
            merchandise.getName(),
            merchandise.getUnit()
        );
    }
}
