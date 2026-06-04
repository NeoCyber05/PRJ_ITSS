package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;
import org.itss.prj_itss.model.request.application.sales.view.RequestReadOnlyView;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.util.List;
import java.util.Objects;

public final class SalesRequestQueryService {

    private final SalesRequestQueryPort queryPort;
    private final MerchandiseUseCase merchandiseUseCase;
    private final RequestDisplayFormatter formatter;

    public SalesRequestQueryService(
            SalesRequestQueryPort queryPort,
            MerchandiseUseCase merchandiseUseCase,
            RequestDisplayFormatter formatter
    ) {
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort");
        this.merchandiseUseCase = Objects.requireNonNull(merchandiseUseCase, "merchandiseUseCase");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return merchandiseUseCase.findActive().stream()
            .map(m -> new MerchandiseOption(m.getId(), m.getCode(), m.getName(), m.getUnit()))
            .toList();
    }

    public MerchandiseOption findMerchandiseOptionByCode(String code) {
        Merchandise m = merchandiseUseCase.findByCode(code);
        if (m == null) return null;
        return new MerchandiseOption(m.getId(), m.getCode(), m.getName(), m.getUnit());
    }

    public MerchandiseOption findMerchandiseOptionById(int id) {
        Merchandise m = merchandiseUseCase.findById(id);
        if (m == null) return null;
        return new MerchandiseOption(m.getId(), m.getCode(), m.getName(), m.getUnit());
    }

    public RequestReadOnlyView findReadOnlyView(int requestId) {
        Request request = queryPort.findById(requestId);
        if (request == null) return null;

        List<RequestDetailItemRow> itemRows = queryPort.findItemsByRequestId(requestId).stream()
            .map(this::toDetailRow)
            .toList();

        return new RequestReadOnlyView(
            request.getId(),
            formatter.formatRequestCode(request.getId()),
            formatter.formatDateOrEmpty(request.getCreatedAt()),
            request.getStatusKey(),
            formatter.requestStatusText(request.getStatusKey()),
            request.getNote(),
            itemRows
        );
    }

    public RequestFormView findFormView(int requestId) {
        Request request = queryPort.findById(requestId);
        if (request == null) return null;

        List<RequestFormView.RequestItemFormRow> itemRows = queryPort.findItemsByRequestId(requestId).stream()
            .map(item -> {
                MerchandiseOption m = findMerchandiseOptionById(item.getMerchandiseId());
                return new RequestFormView.RequestItemFormRow(
                    m,
                    item.getQuantityOrdered() != null ? formatter.formatQuantity(item.getQuantityOrdered()) : "0",
                    formatter.formatDate(item.getDesiredDeliveryDate())
                );
            })
            .toList();

        return new RequestFormView(
            request.getId(),
            formatter.formatRequestCode(request.getId()),
            formatter.formatDateOrEmpty(request.getCreatedAt()),
            request.getStatusKey(),
            formatter.requestStatusText(request.getStatusKey()),
            request.getNote(),
            itemRows
        );
    }

    private RequestDetailItemRow toDetailRow(RequestMerchandise item) {
        MerchandiseOption m = findMerchandiseOptionById(item.getMerchandiseId());
        return new RequestDetailItemRow(
            m != null ? m.code() : "N/A",
            m != null ? m.name() : "N/A",
            item.getQuantityOrdered() != null ? formatter.formatQuantity(item.getQuantityOrdered()) : "0",
            m != null ? m.unit() : "N/A",
            formatter.formatDate(item.getDesiredDeliveryDate())
        );
    }
}
