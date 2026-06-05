package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;

import java.util.List;

public class CreateSalesRequestService implements CreateSalesRequestUseCase {

    private final SalesRequestCommandPort commandPort;
    private final InventoryRepository inventoryRepository;

    public CreateSalesRequestService(SalesRequestCommandPort commandPort, InventoryRepository inventoryRepository) {
        this.commandPort = commandPort;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public int createRequest(List<SalesRequestItemSubmission> items, String note) throws Exception {
        Request request = new Request(note);
        for (SalesRequestItemSubmission item : items) {
            int stock = inventoryRepository.getTotalStock(item.merchandiseId());
            if (item.quantityOrdered().compareTo(new java.math.BigDecimal(stock)) > 0) {
                throw new IllegalArgumentException("Số lượng đặt hàng vượt quá tồn kho hiện tại.");
            }
            request.addItem(item.merchandiseId(), item.quantityOrdered(), item.desiredDeliveryDate());
        }
        return commandPort.createRequest(request);
    }
}
