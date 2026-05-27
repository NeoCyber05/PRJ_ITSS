package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.request.application.port.RequestProcessingGateway;
import org.itss.prj_itss.model.request.application.port.RequestProcessingGatewayException;
import org.itss.prj_itss.model.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.model.request.domain.allocation.model.AllocationPlan;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.request.application.port.RequestRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JdbcRequestProcessingGateway implements RequestProcessingGateway {

    private final RequestRepository requestRepository;
    private final OrderRepository orderRepository;
    private final SiteRepository siteRepository;
    private final InventoryRepository inventoryRepository;
    private final MerchandiseRepository merchandiseRepository;
    private final TransactionRunner transactionRunner;

    public JdbcRequestProcessingGateway(
        RequestRepository requestRepository,
        OrderRepository orderRepository,
        SiteRepository siteRepository,
        InventoryRepository inventoryRepository,
        MerchandiseRepository merchandiseRepository,
        TransactionRunner transactionRunner
    ) {
        this.requestRepository = requestRepository;
        this.orderRepository = orderRepository;
        this.siteRepository = siteRepository;
        this.inventoryRepository = inventoryRepository;
        this.merchandiseRepository = merchandiseRepository;
        this.transactionRunner = transactionRunner;
    }

    @Override
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

    @Override
    public void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws RequestProcessingGatewayException {
        try {
            transactionRunner.execute(() -> {
                for (Map.Entry<Integer, List<Allocation>> siteEntry : AllocationPlan.using(allocations).groupBySite().entrySet()) {
                    Order order = new Order();
                    order.setRequestId(requestId);
                    order.setSiteId(siteEntry.getKey());
                    order.setStatus("pending");

                    int orderId = orderRepository.create(order);
                    if (orderId <= 0) {
                        throw new TransactionException("Cannot create order for site " + siteEntry.getKey());
                    }

                    for (Allocation allocation : siteEntry.getValue()) {
                        OrderMerchandise orderItem = new OrderMerchandise(
                            orderId,
                            allocation.merchandiseId,
                            BigDecimal.valueOf(allocation.getQuantity()),
                            toStoredDeliveryMethod(allocation.transport)
                        );
                        if (!orderRepository.addItem(orderItem)) {
                            throw new TransactionException("Cannot create order line for order " + orderId);
                        }
                    }
                }

                if (!requestRepository.updateStatus(requestId, RequestStatus.PROCESSING)) {
                    throw new TransactionException("Cannot update request status " + requestId);
                }
            });
        } catch (TransactionException exception) {
            throw new RequestProcessingGatewayException(
                "Cannot create allocated orders for request " + requestId,
                exception
            );
        }
    }

    private String toStoredDeliveryMethod(String transport) {
        DeliveryMethod method = DeliveryMethod.fromRaw(transport);
        return (method == null ? DeliveryMethod.SHIP : method).storageValue();
    }
}

