package org.itss.prj_itss.model.order.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGateway;
import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGatewayException;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.domain.cancellation.CancelledOrderProcessingData;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationPlan;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.domain.Site;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JdbcCancelledOrderProcessingGateway implements CancelledOrderProcessingGateway {

    private final OrderRepository orderRepository;
    private final SiteRepository siteRepository;
    private final InventoryRepository inventoryRepository;
    private final ProcessingRequestPort requestRepository;
    private final MerchandiseRepository merchandiseRepository;
    private final TransactionRunner transactionRunner;

    public JdbcCancelledOrderProcessingGateway(
        OrderRepository orderRepository,
        SiteRepository siteRepository,
        InventoryRepository inventoryRepository,
        ProcessingRequestPort requestRepository,
        MerchandiseRepository merchandiseRepository,
        TransactionRunner transactionRunner
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
        this.siteRepository = Objects.requireNonNull(siteRepository, "siteRepository");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository");
        this.requestRepository = Objects.requireNonNull(requestRepository, "requestRepository");
        this.merchandiseRepository = Objects.requireNonNull(merchandiseRepository, "merchandiseRepository");
        this.transactionRunner = Objects.requireNonNull(transactionRunner, "transactionRunner");
    }

    @Override
    public CancelledOrderProcessingData loadProcessingData(int cancelledOrderId)
            throws CancelledOrderProcessingGatewayException {
        try {
            Order cancelledOrder = orderRepository.findById(cancelledOrderId);
            if (cancelledOrder == null) {
                throw new CancelledOrderProcessingGatewayException("Cancelled order not found: " + cancelledOrderId);
            }

            int requestId = cancelledOrder.getRequestId();
            int cancelledSiteId = cancelledOrder.getSiteId();
            String requestCode = OrderingFormatters.formatRequestCode(requestId);

            List<OrderMerchandise> orderItems = orderRepository.findItemsByOrderId(cancelledOrderId);
            List<ItemRequirement> items = new ArrayList<>();
            Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();

            List<RequestMerchandise> requestItems = requestRepository.findItemsByRequestId(requestId);
            Map<Integer, LocalDate> requestDesiredDates = new LinkedHashMap<>();
            for (RequestMerchandise ri : requestItems) {
                requestDesiredDates.put(ri.getMerchandiseId(), ri.getDesiredDeliveryDate());
            }

            List<Merchandise> allMerch = merchandiseRepository.findAll();
            Map<Integer, Merchandise> merchMap = allMerch.stream()
                .collect(java.util.stream.Collectors.toMap(Merchandise::getId, java.util.function.Function.identity(), (a, b) -> a, LinkedHashMap::new));

            for (OrderMerchandise orderItem : orderItems) {
                Merchandise merchandise = merchMap.get(orderItem.getMerchandiseId());
                if (merchandise != null) {
                    int quantity = orderItem.getQuantity().intValue();
                    items.add(new ItemRequirement(
                        merchandise.getId(),
                        merchandise.getCode(),
                        merchandise.getName(),
                        quantity
                    ));
                    LocalDate desiredDate = requestDesiredDates.get(merchandise.getId());
                    if (desiredDate != null) {
                        desiredDeliveryDates.put(merchandise.getId(), desiredDate);
                    }
                }
            }

            LocalDate desiredDeliveryDate = null;
            for (ItemRequirement item : items) {
                LocalDate itemDate = desiredDeliveryDates.get(item.merchandiseId);
                if (itemDate != null) {
                    if (desiredDeliveryDate == null || itemDate.isBefore(desiredDeliveryDate)) {
                        desiredDeliveryDate = itemDate;
                    }
                }
            }

            if (desiredDeliveryDate == null) {
                desiredDeliveryDate = LocalDate.now().plusDays(14);
            }

            int deadlineDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), desiredDeliveryDate);
            if (deadlineDays < 1) {
                deadlineDays = 1;
            }

            List<Integer> merchandiseIds = items.stream().map(i -> i.merchandiseId).distinct().toList();
            List<SiteStockOption> sites = new ArrayList<>();
            for (Site site : siteRepository.findAvailableForMerchandiseIds(merchandiseIds)) {
                sites.add(new SiteStockOption(
                    site.getId(),
                    site.getSiteCode(),
                    site.getName(),
                    site.getDescription(),
                    site.getShipDeliveryDays(),
                    site.getAirDeliveryDays(),
                    inventoryRepository.getInventoryBySiteId(site.getId())
                ));
            }

            return new CancelledOrderProcessingData(
                cancelledOrderId,
                requestId,
                cancelledSiteId,
                requestCode,
                desiredDeliveryDate,
                deadlineDays,
                items,
                sites
            );
        } catch (Exception exception) {
            throw new CancelledOrderProcessingGatewayException(
                "Failed to load processing data for cancelled order " + cancelledOrderId,
                exception
            );
        }
    }

    @Override
    public void createAllocatedOrders(
        int cancelledOrderId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws CancelledOrderProcessingGatewayException {
        try {
            transactionRunner.execute(() -> {
                Order cancelledOrder = orderRepository.findById(cancelledOrderId);
                if (cancelledOrder == null) {
                    throw new TransactionException("Cancelled order not found: " + cancelledOrderId);
                }

                // Confirm status is updated to removed
                if (!orderRepository.updateStatus(cancelledOrderId, OrderingFormatters.STATUS_REMOVED)) {
                    throw new TransactionException("Failed to update cancelled order status: " + cancelledOrderId);
                }

                // Group allocations by site
                for (Map.Entry<Integer, List<Allocation>> siteEntry : AllocationPlan.using(allocations).groupBySite().entrySet()) {
                    Order newOrder = new Order();
                    newOrder.setRequestId(cancelledOrder.getRequestId());
                    newOrder.setSiteId(siteEntry.getKey());
                    newOrder.setStatus("pending"); // pending corresponds to CHO_XAC_NHAN

                    int newOrderId = orderRepository.create(newOrder);
                    if (newOrderId <= 0) {
                        throw new TransactionException("Cannot create new order for site " + siteEntry.getKey());
                    }

                    for (Allocation allocation : siteEntry.getValue()) {
                        OrderMerchandise orderItem = new OrderMerchandise(
                            newOrderId,
                            allocation.merchandiseId,
                            BigDecimal.valueOf(allocation.getQuantity()),
                            toStoredDeliveryMethod(allocation.transport)
                        );
                        if (!orderRepository.addItem(orderItem)) {
                            throw new TransactionException("Cannot create order line for order " + newOrderId);
                        }
                    }
                }
            });
        } catch (Exception exception) {
            throw new CancelledOrderProcessingGatewayException(
                "Failed to create allocated orders for cancelled order " + cancelledOrderId,
                exception
            );
        }
    }

    private String toStoredDeliveryMethod(String transport) {
        DeliveryMethod method = DeliveryMethod.fromRaw(transport);
        return (method == null ? DeliveryMethod.SHIP : method).storageValue();
    }
}
