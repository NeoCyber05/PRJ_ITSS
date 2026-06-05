package org.itss.prj_itss.controller.warehouse;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.ConfirmationResult;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.InspectionItemInput;
import org.itss.prj_itss.model.warehouse.domain.InspectionResult;
import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.controller.shared.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class ConfirmOrderArrivalController {
    private final WarehouseReceivingUseCase warehouseReceivingUseCase;
    private final SiteUseCase siteUseCase;
    private final MerchandiseUseCase MerchandiseUseCase;

    public ConfirmOrderArrivalController(
            WarehouseReceivingUseCase warehouseReceivingUseCase,
            SiteUseCase siteUseCase,
            MerchandiseUseCase MerchandiseUseCase) {
        this.warehouseReceivingUseCase = warehouseReceivingUseCase;
        this.siteUseCase = siteUseCase;
        this.MerchandiseUseCase = MerchandiseUseCase;
    }

    public List<Order> findInboundOrders() {
        return warehouseReceivingUseCase.findInboundOrders();
    }

    public List<OrderMerchandise> findItemsByOrderId(int orderId) {
        return warehouseReceivingUseCase.findItemsByOrderId(orderId);
    }

    public Site findSiteById(int siteId) {
        return siteUseCase.findById(siteId);
    }

    public Merchandise findMerchandiseById(int merchandiseId) {
        return MerchandiseUseCase.findById(merchandiseId);
    }

    public ValidationResult validateInspection(List<InspectionItemDto> items, String overallNote) {
        List<String> errors = new ArrayList<>();
        boolean hasDiscrepancy = false;

        for (InspectionItemDto item : items) {
            String rawQuantity = item.receivedQuantityInput() == null ? "" : item.receivedQuantityInput().trim();
            if (rawQuantity.isEmpty()) {
                errors.add("Số lượng thực nhận không được để trống.");
                return new ValidationResult(false, errors);
            }

            Integer receivedQuantity = parseReceivedQuantity(rawQuantity);
            if (receivedQuantity == null) {
                errors.add("Số lượng thực nhận phải là số nguyên không âm.");
                return new ValidationResult(false, errors);
            }

            InspectionResult inspectionResult = item.inspectionResult();
            if (inspectionResult == null) {
                errors.add("Vui lòng chọn kết quả kiểm nhận cho từng mặt hàng.");
                return new ValidationResult(false, errors);
            }

            if (receivedQuantity != item.orderedQuantity() || inspectionResult.indicatesDiscrepancy()) {
                hasDiscrepancy = true;
            }
        }

        String normalizedNote = overallNote == null ? "" : overallNote.trim();
        if (hasDiscrepancy && normalizedNote.isEmpty()) {
            errors.add("Có chênh lệch, hãy viết ghi chú chênh lệch.");
            return new ValidationResult(false, errors);
        }

        return new ValidationResult(true, errors);
    }

    public ConfirmationResult confirmArrival(int orderId, List<InspectionItemDto> items, String overallNote) {
        List<InspectionItemInput> itemInputs = new ArrayList<>();
        for (InspectionItemDto item : items) {
            Integer receivedQuantity = parseReceivedQuantity(item.receivedQuantityInput());
            itemInputs.add(new InspectionItemInput(
                item.merchandiseId(),
                receivedQuantity,
                item.inspectionResult(),
                ""
            ));
        }
        return warehouseReceivingUseCase.confirmArrival(orderId, itemInputs, overallNote);
    }

    private Integer parseReceivedQuantity(String rawQuantity) {
        if (rawQuantity == null) {
            return null;
        }
        String normalized = rawQuantity.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public record InspectionItemDto(
        int merchandiseId,
        int orderedQuantity,
        String receivedQuantityInput,
        InspectionResult inspectionResult
    ) {}
}
