package org.itss.prj_itss.model.warehouse.application;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.warehouse.domain.WarehouseReceipt;
import org.itss.prj_itss.model.warehouse.domain.WarehouseReceiptItem;
import org.itss.prj_itss.model.warehouse.domain.InspectionResult;
import org.itss.prj_itss.model.order.domain.OrderStatus;
import org.itss.prj_itss.model.warehouse.application.port.WarehouseReceiptRepository;
import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.site.application.SiteUseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class WarehouseReceivingUseCase {

    private final OrderRepository orderRepository;
    private final SiteUseCase siteService;
    private final MerchandiseUseCase merchandiseService;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final TransactionRunner warehouseTransactionRunner;
    private final Supplier<AuthenticatedUser> authenticatedUserSupplier;

    public WarehouseReceivingUseCase(
        OrderRepository orderRepository,
        SiteUseCase siteService,
        MerchandiseUseCase merchandiseService,
        WarehouseReceiptRepository warehouseReceiptRepository,
        TransactionRunner warehouseTransactionRunner,
        Supplier<AuthenticatedUser> authenticatedUserSupplier
    ) {
        this.orderRepository = orderRepository;
        this.siteService = siteService;
        this.merchandiseService = merchandiseService;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.warehouseTransactionRunner = warehouseTransactionRunner;
        this.authenticatedUserSupplier = authenticatedUserSupplier;
    }

    public List<Order> findInboundOrders() {
        return orderRepository.findByStatus(OrderStatus.SHIPPING.displayValue()).stream()
            .sorted(Comparator
                .comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Comparator.comparingInt(Order::getId).reversed()))
            .toList();
    }

    public List<OrderMerchandise> findItemsByOrderId(int orderId) {
        return orderRepository.findItemsByOrderId(orderId);
    }

    public ConfirmationResult confirmArrival(int orderId, List<InspectionItemInput> itemInputs, String overallNote) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return ConfirmationResult.failure("Không tìm thấy đơn hàng.");
        }
        if (!OrderStatus.SHIPPING.displayValue().equalsIgnoreCase(order.getStatus())) {
            return ConfirmationResult.failure("Chỉ có thể xác nhận đơn hàng đang giao.");
        }

        List<OrderMerchandise> orderItems = orderRepository.findItemsByOrderId(orderId);
        if (orderItems.isEmpty()) {
            return ConfirmationResult.failure("Đơn hàng không có mặt hàng để kiểm nhận.");
        }

        Site site = siteService.findById(order.getSiteId());
        if (site == null) {
            return ConfirmationResult.failure("Không tìm thấy thông tin site của đơn hàng.");
        }

        Map<Integer, OrderMerchandise> orderItemsByMerchandiseId = new LinkedHashMap<>();
        for (OrderMerchandise orderItem : orderItems) {
            orderItemsByMerchandiseId.put(orderItem.getMerchandiseId(), orderItem);
        }

        if (itemInputs.size() != orderItems.size()) {
            return ConfirmationResult.failure("Dữ liệu kiểm nhận không đầy đủ.");
        }

        String normalizedOverallNote = normalizeText(overallNote);
        boolean hasDiscrepancy = false;
        String discrepancyNote = "";

        for (InspectionItemInput input : itemInputs) {
            OrderMerchandise orderItem = orderItemsByMerchandiseId.get(input.merchandiseId());
            if (orderItem == null) {
                return ConfirmationResult.failure("Có mặt hàng kiểm nhận không thuộc đơn hàng.");
            }
            if (input.receivedQuantity() == null) {
                return ConfirmationResult.failure("Số lượng thực nhận không được để trống.");
            }
            if (input.receivedQuantity() < 0) {
                return ConfirmationResult.failure("Số lượng thực nhận không được âm.");
            }
            if (input.inspectionResult() == null) {
                return ConfirmationResult.failure("Vui lòng chọn kết quả kiểm nhận cho từng mặt hàng.");
            }

            BigDecimal orderedQuantity = orderItem.getQuantity() == null ? BigDecimal.ZERO : orderItem.getQuantity();
            boolean itemHasDiscrepancy = orderedQuantity.intValue() != input.receivedQuantity()
                || input.inspectionResult().indicatesDiscrepancy();
            if (itemHasDiscrepancy) {
                hasDiscrepancy = true;
                if (normalizeText(input.itemNote()).isBlank() && normalizedOverallNote.isBlank()) {
                    return ConfirmationResult.failure("Có chênh lệch, hãy viết ghi chú chênh lệch.");
                }
            }
        }

        if (hasDiscrepancy) {
            discrepancyNote = buildDiscrepancyNote(itemInputs, orderItemsByMerchandiseId, normalizedOverallNote);
        }

        AuthenticatedUser authenticatedUser = authenticatedUserSupplier == null ? null : authenticatedUserSupplier.get();
        String finalDiscrepancyNote = discrepancyNote;
        boolean finalHasDiscrepancy = hasDiscrepancy;

        try {
            warehouseTransactionRunner.execute(() -> {
                WarehouseReceipt receipt = buildWarehouseReceipt(
                    order,
                    site,
                    normalizedOverallNote,
                    finalHasDiscrepancy,
                    finalDiscrepancyNote,
                    authenticatedUser
                );

                int receiptId = warehouseReceiptRepository.createReceipt(receipt);
                if (receiptId <= 0) {
                    throw new TransactionException("Không thể tạo phiếu kiểm nhận trong DB kho.");
                }

                for (InspectionItemInput input : itemInputs) {
                    OrderMerchandise orderItem = orderItemsByMerchandiseId.get(input.merchandiseId());
                    Merchandise merchandise = merchandiseService.findById(input.merchandiseId());
                    if (merchandise == null) {
                        throw new TransactionException("Không tìm thấy thông tin mặt hàng để lưu snapshot DB kho.");
                    }

                    WarehouseReceiptItem receiptItem = buildWarehouseReceiptItem(orderItem, merchandise, receiptId, input);
                    if (!warehouseReceiptRepository.addReceiptItem(receiptItem)) {
                        throw new TransactionException("Không thể lưu chi tiết kiểm nhận trong DB kho.");
                    }
                }

                if (!orderRepository.updateStatus(orderId, OrderStatus.DELIVERED.displayValue())) {
                    throw new TransactionException("Không thể cập nhật trạng thái đơn hàng ở DB chính.");
                }
            });
        } catch (TransactionException exception) {
            return ConfirmationResult.failure(exception.getMessage());
        }

        return ConfirmationResult.success(normalizeText(finalDiscrepancyNote));
    }

    private WarehouseReceipt buildWarehouseReceipt(
        Order order,
        Site site,
        String overallNote,
        boolean hasDiscrepancy,
        String discrepancyNote,
        AuthenticatedUser authenticatedUser
    ) {
        WarehouseReceipt receipt = new WarehouseReceipt();
        receipt.setSourceOrderId(order.getId());
        receipt.setSourceOrderCode(formatOrderCode(order.getId()));
        receipt.setSourceRequestCode(formatRequestCode(order.getRequestId()));
        receipt.setSiteCode(safeText(site.getSiteCode()));
        receipt.setSiteName(safeText(site.getName()));
        receipt.setResultStatus(OrderStatus.DELIVERED.displayValue());
        receipt.setHasDiscrepancy(hasDiscrepancy);
        receipt.setDiscrepancyNote(discrepancyNote);
        receipt.setOverallNote(overallNote);
        receipt.setConfirmedAt(LocalDateTime.now());

        if (authenticatedUser != null) {
            receipt.setConfirmedByAccountId(authenticatedUser.account().getId());
            receipt.setConfirmedByUsername(authenticatedUser.username());
        }
        return receipt;
    }

    private WarehouseReceiptItem buildWarehouseReceiptItem(
        OrderMerchandise orderItem,
        Merchandise merchandise,
        int receiptId,
        InspectionItemInput input
    ) {
        WarehouseReceiptItem receiptItem = new WarehouseReceiptItem();
        receiptItem.setReceiptId(receiptId);
        receiptItem.setSourceOrderItemId(orderItem.getMerchandiseId());
        receiptItem.setMerchandiseCode(safeText(merchandise.getCode()));
        receiptItem.setMerchandiseName(safeText(merchandise.getName()));
        receiptItem.setOrderedQuantity(orderItem.getQuantity());
        receiptItem.setReceivedQuantity(BigDecimal.valueOf(input.receivedQuantity()));
        receiptItem.setUnit(safeText(merchandise.getUnit()));
        receiptItem.setTransportMethod(normalizeTransportMethod(orderItem.getDeliveryMethod()));
        receiptItem.setInspectionResult(input.inspectionResult().storedValue());
        receiptItem.setItemNote(normalizeText(input.itemNote()));
        return receiptItem;
    }

    private String buildDiscrepancyNote(
        List<InspectionItemInput> itemInputs,
        Map<Integer, OrderMerchandise> orderItemsByMerchandiseId,
        String overallNote
    ) {
        if (!overallNote.isBlank()) {
            return overallNote;
        }
        return itemInputs.stream()
            .filter(input -> {
                OrderMerchandise orderItem = orderItemsByMerchandiseId.get(input.merchandiseId());
                int orderedQuantity = orderItem == null || orderItem.getQuantity() == null
                    ? 0
                    : orderItem.getQuantity().intValue();
                return orderedQuantity != input.receivedQuantity() || input.inspectionResult().indicatesDiscrepancy();
            })
            .map(input -> normalizeText(input.itemNote()))
            .filter(note -> !note.isBlank())
            .collect(Collectors.joining("; "));
    }

    private String normalizeTransportMethod(String deliveryMethod) {
        return safeText(deliveryMethod);
    }

    private String formatOrderCode(int orderId) {
        return String.format("ĐH-2026-%03d", orderId);
    }

    private String formatRequestCode(int requestId) {
        return String.format("YC-2026-%03d", requestId);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    public record InspectionItemInput(
        int merchandiseId,
        Integer receivedQuantity,
        InspectionResult inspectionResult,
        String itemNote
    ) {
    }

    public record ConfirmationResult(boolean success, String message, String discrepancyNote) {
        public static ConfirmationResult success(String discrepancyNote) {
            return new ConfirmationResult(true, "", discrepancyNote);
        }

        public static ConfirmationResult failure(String message) {
            return new ConfirmationResult(false, message, "");
        }
    }
}
