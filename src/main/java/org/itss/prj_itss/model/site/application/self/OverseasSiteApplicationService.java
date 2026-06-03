package org.itss.prj_itss.model.site.application.self;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.SiteOrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.application.port.SiteInventoryCommandPort;
import org.itss.prj_itss.model.site.application.port.SiteProfileCommandPort;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OverseasSiteApplicationService {

    private final SiteUseCase siteUseCase;
    private final CatalogUseCase catalogUseCase;
    private final SiteProfileCommandPort profileCommandPort;
    private final SiteInventoryCommandPort inventoryCommandPort;
    private final SiteOrderRepository siteOrderRepository;

    public OverseasSiteApplicationService(
        SiteUseCase siteUseCase,
        CatalogUseCase catalogUseCase,
        SiteProfileCommandPort profileCommandPort,
        SiteInventoryCommandPort inventoryCommandPort,
        SiteOrderRepository siteOrderRepository
    ) {
        this.siteUseCase = Objects.requireNonNull(siteUseCase, "siteUseCase");
        this.catalogUseCase = Objects.requireNonNull(catalogUseCase, "catalogUseCase");
        this.profileCommandPort = Objects.requireNonNull(profileCommandPort, "profileCommandPort");
        this.inventoryCommandPort = Objects.requireNonNull(inventoryCommandPort, "inventoryCommandPort");
        this.siteOrderRepository = Objects.requireNonNull(siteOrderRepository, "siteOrderRepository");
    }

    public SiteWorkspaceSnapshot load(int siteId) {
        Site site = siteUseCase.findById(siteId);
        if (site == null) {
            return SiteWorkspaceSnapshot.unavailable("Site không tồn tại.");
        }

        Map<Integer, Integer> inventory = siteUseCase.getInventoryBySiteId(siteId);
        List<Merchandise> merchandise = catalogUseCase.findAll();
        List<SiteInventoryRow> inventoryRows = merchandise.stream()
            .filter(item -> inventory.containsKey(item.getId()))
            .map(item -> new SiteInventoryRow(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getUnit(),
                inventory.getOrDefault(item.getId(), 0)
            ))
            .toList();

        List<SiteOrderRow> orderRows = siteOrderRepository.findBySiteId(siteId).stream()
            .map(this::toOrderRow)
            .toList();

        return new SiteWorkspaceSnapshot(true, "", site, merchandise, inventoryRows, orderRows);
    }

    public SiteWorkspaceResult updateProfile(int siteId, SiteProfileDraft draft) {
        Site site = siteUseCase.findById(siteId);
        if (site == null) {
            return SiteWorkspaceResult.failure("Site không tồn tại.");
        }
        if (draft.name() == null || draft.name().isBlank()) {
            return SiteWorkspaceResult.failure("Tên site không được để trống.");
        }
        if (draft.shipDeliveryDays() != null && draft.shipDeliveryDays() < 0) {
            return SiteWorkspaceResult.failure("Ngày vận chuyển đường biển không hợp lệ.");
        }
        if (draft.airDeliveryDays() != null && draft.airDeliveryDays() < 0) {
            return SiteWorkspaceResult.failure("Ngày vận chuyển đường hàng không không hợp lệ.");
        }
        profileCommandPort.updateProfile(siteId, draft);
        return SiteWorkspaceResult.success("Cập nhật thông tin site thành công.");
    }

    public SiteWorkspaceResult updateInventoryItem(int siteId, SiteInventoryDraft draft) {
        if (siteUseCase.findById(siteId) == null) {
            return SiteWorkspaceResult.failure("Site không tồn tại.");
        }
        if (catalogUseCase.findById(draft.merchandiseId()) == null) {
            return SiteWorkspaceResult.failure("Mặt hàng không tồn tại.");
        }
        if (draft.stockQuantity() < 0) {
            return SiteWorkspaceResult.failure("Số lượng tồn kho không được âm.");
        }
        inventoryCommandPort.upsertInventoryItem(siteId, draft.merchandiseId(), draft.stockQuantity());
        return SiteWorkspaceResult.success("Cập nhật tồn kho thành công.");
    }

    public SiteWorkspaceResult removeInventoryItem(int siteId, int merchandiseId) {
        if (siteUseCase.findById(siteId) == null) {
            return SiteWorkspaceResult.failure("Site không tồn tại.");
        }
        inventoryCommandPort.removeInventoryItem(siteId, merchandiseId);
        return SiteWorkspaceResult.success("Đã bỏ mặt hàng khỏi danh sách kinh doanh.");
    }

    public SiteWorkspaceResult confirmSupply(int siteId, int orderId) {
        Site site = siteUseCase.findById(siteId);
        if (site == null) {
            return SiteWorkspaceResult.failure("Site không tồn tại.");
        }

        Order order = siteOrderRepository.findByIdForSite(orderId, siteId);
        if (order == null) {
            return SiteWorkspaceResult.failure("Đơn hàng không thuộc site này.");
        }

        String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
        if (!OrderingFormatters.STATUS_PENDING.equals(statusKey)) {
            return SiteWorkspaceResult.failure("Chỉ có thể xác nhận đơn hàng đang chờ xác nhận.");
        }

        boolean updated = siteOrderRepository.updateStatusForSite(orderId, siteId, OrderingFormatters.STATUS_SHIPPING);
        if (!updated) {
            return SiteWorkspaceResult.failure("Không thể cập nhật trạng thái đơn hàng.");
        }

        return SiteWorkspaceResult.success("Đã xác nhận cung ứng đơn hàng.");
    }

    public List<SiteOrderItemRow> loadOrderItems(int siteId, int orderId) {
        Order order = siteOrderRepository.findByIdForSite(orderId, siteId);
        if (order == null) {
            return List.of();
        }
        return siteOrderRepository.findItemsByOrderId(orderId).stream()
            .map(this::toOrderItemRow)
            .toList();
    }

    private SiteOrderRow toOrderRow(Order order) {
        String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
        return new SiteOrderRow(
            order.getId(),
            order.getRequestId(),
            OrderingFormatters.formatOrderCode(order.getId()),
            OrderingFormatters.formatRequestCode(order.getRequestId()),
            OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
            order.getStatus(),
            OrderingFormatters.orderStatusText(order.getStatus()),
            OrderingFormatters.STATUS_PENDING.equals(statusKey)
        );
    }

    private SiteOrderItemRow toOrderItemRow(OrderMerchandise item) {
        Merchandise merchandise = catalogUseCase.findById(item.getMerchandiseId());
        return new SiteOrderItemRow(
            item.getMerchandiseId(),
            merchandise == null ? "N/A" : merchandise.getCode(),
            merchandise == null ? "N/A" : merchandise.getName(),
            merchandise == null ? "N/A" : merchandise.getUnit(),
            OrderingFormatters.formatQuantity(item.getQuantity()),
            OrderingFormatters.deliveryMethodText(item.getDeliveryMethod())
        );
    }
}
