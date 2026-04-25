package org.itss.prj_itss.service;

import org.itss.prj_itss.common.config.ITransactionRunner;
import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.RequestProcessingData;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.repository.IInventoryRepository;
import org.itss.prj_itss.repository.IMerchandiseRepository;
import org.itss.prj_itss.repository.IOrderRepository;
import org.itss.prj_itss.repository.IRequestRepository;
import org.itss.prj_itss.repository.ISiteRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RequestProcessingService {

    private final IRequestRepository requestRepository;
    private final IOrderRepository orderRepository;
    private final ISiteRepository siteRepository;
    private final IInventoryRepository inventoryRepository;
    private final IMerchandiseRepository merchandiseRepository;
    private final ITransactionRunner transactionRunner;

    public RequestProcessingService(
        IRequestRepository requestRepository,
        IOrderRepository orderRepository,
        ISiteRepository siteRepository,
        IInventoryRepository inventoryRepository,
        IMerchandiseRepository merchandiseRepository,
        ITransactionRunner transactionRunner
    ) {
        this.requestRepository = requestRepository;
        this.orderRepository = orderRepository;
        this.siteRepository = siteRepository;
        this.inventoryRepository = inventoryRepository;
        this.merchandiseRepository = merchandiseRepository;
        this.transactionRunner = transactionRunner;
    }

    public RequestProcessingData loadProcessingData(int requestId) {
        List<ItemRequirement> items = new ArrayList<>();
        Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();
        for (RequestMerchandise requestItem : requestRepository.findItemsByRequestId(requestId)) {
            Merchandise merchandise = merchandiseRepository.findById(requestItem.getMerchandiseId());
            if (merchandise != null) {
                items.add(new ItemRequirement(
                    merchandise.getId(),
                    merchandise.getCode(),
                    merchandise.getName(),
                    requestItem.getQuantityOrdered().intValue()
                ));
                desiredDeliveryDates.put(merchandise.getId(), requestItem.getDesiredDeliveryDate());
            }
        }

        LocalDate earliestDeliveryDate = requestRepository.getEarliestDeliveryDate(requestId);
        int deadlineDays = 14;
        if (earliestDeliveryDate != null) {
            deadlineDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), earliestDeliveryDate);
            if (deadlineDays < 1) {
                deadlineDays = 1;
            }
        }

        List<Integer> requestedMerchandiseIds = items.stream()
            .map(item -> item.merchandiseId)
            .distinct()
            .toList();
        List<SiteStockOption> sites = new ArrayList<>();
        for (Site site : siteRepository.findAvailableForMerchandiseIds(requestedMerchandiseIds)) {
            sites.add(new SiteStockOption(
                site.getId(),
                site.getSiteCode(),
                site.getName(),
                site.getDescription(),
                site.getShipDeliveryDays() == null ? 999 : site.getShipDeliveryDays(),
                site.getAirDeliveryDays() == null ? 999 : site.getAirDeliveryDays(),
                inventoryRepository.getInventoryBySiteId(site.getId())
            ));
        }

        return new RequestProcessingData(requestId, earliestDeliveryDate, deadlineDays, items, sites, desiredDeliveryDates);
    }

    public List<String> validateAllocations(
        List<ItemRequirement> items,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        List<String> errors = new ArrayList<>();
        for (ItemRequirement item : items) {
            int allocated = allocatedQuantity(allocations, item.merchandiseId);
            if (allocated < item.required) {
                errors.add("- " + item.code + " chỉ phân bổ " + allocated + "/" + item.required);
            }
            if (allocated > item.required) {
                errors.add("- " + item.code + " phân bổ vượt " + allocated + "/" + item.required);
            }
        }
        return errors;
    }

    public String validateSubmission(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates,
        int deadlineDays
    ) {
        for (ItemRequirement item : items) {
            int allocated = allocatedQuantity(allocations, item.merchandiseId);
            if (allocated < item.required) {
                return "Chưa đủ số lượng hàng cần";
            }
            if (allocated > item.required) {
                return "Số lượng phân bổ vượt yêu cầu";
            }
        }

        for (ItemRequirement item : items) {
            LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
            int itemDeadlineDays = desiredDate == null
                ? deadlineDays
                : Math.max(1, (int) ChronoUnit.DAYS.between(LocalDate.now(), desiredDate));

            Map<Integer, Allocation> itemAllocations = allocations.getOrDefault(item.merchandiseId, Map.of());
            for (Allocation allocation : itemAllocations.values()) {
                SiteStockOption site = findSite(allSites, allocation.siteId);
                if (site == null) {
                    return "Không đáp ứng ngày nhận mong muốn";
                }

                int deliveryDays = deliveryDays(site, allocation.transport);
                if (deliveryDays >= 999 || deliveryDays > itemDeadlineDays) {
                    return "Không đáp ứng ngày nhận mong muốn";
                }
            }
        }

        return null;
    }

    public void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws SQLException {
        transactionRunner.execute(() -> {
            for (Map.Entry<Integer, List<Allocation>> siteEntry : groupAllocationsBySite(allocations).entrySet()) {
                Order order = new Order();
                order.setRequestId(requestId);
                order.setSiteId(siteEntry.getKey());
                order.setStatus("Chờ xác nhận");

                int orderId = orderRepository.create(order);
                if (orderId <= 0) {
                    throw new SQLException("Cannot create order for site " + siteEntry.getKey());
                }

                for (Allocation allocation : siteEntry.getValue()) {
                    OrderMerchandise orderItem = new OrderMerchandise(
                        orderId,
                        allocation.merchandiseId,
                        BigDecimal.valueOf(allocation.getQuantity()),
                        toStoredDeliveryMethod(allocation.transport)
                    );
                    if (!orderRepository.addItem(orderItem)) {
                        throw new SQLException("Cannot create order line for order " + orderId);
                    }
                }
            }

            if (!requestRepository.updateStatus(requestId, "Đang xử lý")) {
                throw new SQLException("Cannot update request status " + requestId);
            }
        });
    }

    private int allocatedQuantity(Map<Integer, Map<Integer, Allocation>> allocations, int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Map.of())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    private SiteStockOption findSite(List<SiteStockOption> allSites, int siteId) {
        for (SiteStockOption site : allSites) {
            if (site.id == siteId) {
                return site;
            }
        }
        return null;
    }

    private int deliveryDays(SiteStockOption site, String transport) {
        return isAirTransport(transport) ? site.airDays : site.shipDays;
    }

    private boolean isAirTransport(String transport) {
        if (transport == null) {
            return false;
        }

        String normalized = transport.trim().toLowerCase();
        return normalized.contains("air")
            || normalized.contains("hàng không")
            || normalized.contains("hang khong")
            || normalized.contains("máy")
            || normalized.contains("may");
    }

    private Map<Integer, List<Allocation>> groupAllocationsBySite(
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        Map<Integer, List<Allocation>> groupedAllocations = new LinkedHashMap<>();
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            for (Allocation allocation : itemAllocations.values()) {
                if (allocation.getQuantity() <= 0) {
                    continue;
                }
                groupedAllocations
                    .computeIfAbsent(allocation.siteId, key -> new ArrayList<>())
                    .add(allocation);
            }
        }
        return groupedAllocations;
    }

    private String toStoredDeliveryMethod(String transport) {
        return AllocationPlanningService.TRANSPORT_AIR.equals(transport) ? "air" : "ship";
    }
}
