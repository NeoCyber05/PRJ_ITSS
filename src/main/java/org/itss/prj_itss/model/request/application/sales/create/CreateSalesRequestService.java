package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;

import java.util.List;
import java.util.Map;

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
            if (item.quantityOrdered().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số lượng đặt hàng phải lớn hơn 0.");
            }
        }

        Map<Integer, Integer> stockByMerchandiseId = Map.of();
        if (!items.isEmpty()) {
            List<Integer> merchandiseIds = items.stream()
                .map(SalesRequestItemSubmission::merchandiseId)
                .distinct()
                .toList();
            stockByMerchandiseId = inventoryRepository.getTotalStockByMerchandiseIds(merchandiseIds);
        }

        for (SalesRequestItemSubmission item : items) {
            int stock = stockByMerchandiseId.getOrDefault(item.merchandiseId(), 0);
            if (item.quantityOrdered().compareTo(new java.math.BigDecimal(stock)) > 0) {
                throw new IllegalArgumentException("Số lượng đặt hàng vượt quá tồn kho hiện tại.");
            }
            request.addItem(item.merchandiseId(), item.quantityOrdered(), item.desiredDeliveryDate());
        }
        return commandPort.createRequest(request);
    }
}
