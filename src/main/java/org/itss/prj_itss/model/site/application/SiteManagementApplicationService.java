package org.itss.prj_itss.model.site.application;

import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.port.SiteAccountProvisioningPort;
import org.itss.prj_itss.model.site.application.port.SiteCommandRepository;
import org.itss.prj_itss.model.catalog.application.CatalogUseCase;

import java.util.List;
import java.util.Objects;

public final class SiteManagementApplicationService {

    private final SiteUseCase siteService;
    private final CatalogUseCase merchandiseService;
    private final SiteCommandRepository siteCommandRepository;
    private final SiteAccountProvisioningPort siteAccountProvisioningPort;
    private final TransactionRunner transactionRunner;

    public SiteManagementApplicationService(
            SiteUseCase siteService,
            CatalogUseCase merchandiseService,
            SiteCommandRepository siteCommandRepository,
            SiteAccountProvisioningPort siteAccountProvisioningPort,
            TransactionRunner transactionRunner) {
        this.siteService = Objects.requireNonNull(siteService, "siteService");
        this.merchandiseService = Objects.requireNonNull(merchandiseService, "merchandiseService");
        this.siteCommandRepository = Objects.requireNonNull(siteCommandRepository, "siteCommandRepository");
        this.siteAccountProvisioningPort = Objects.requireNonNull(siteAccountProvisioningPort, "siteAccountProvisioningPort");
        this.transactionRunner = Objects.requireNonNull(transactionRunner, "transactionRunner");
    }

    public Snapshot load() {
        List<Site> sites = siteService.findAll();
        List<SiteRow> rows = sites.stream().map(this::toRow).toList();
        return new Snapshot(rows, sites.size(), sites.size(), merchandiseService.countAll());
    }

    public List<Site> findSites() {
        return siteService.findAll();
    }

    public int countMerchandise() {
        return merchandiseService.countAll();
    }

    public List<SiteRow> loadRows() {
        return load().rows();
    }

    public List<SiteRow> filterRows(List<SiteRow> rows, String keyword) {
        return rows.stream().filter(row -> row.matchesKeyword(keyword)).toList();
    }

    public SiteRow toRow(Site site) {
        int itemCount = siteService.countMerchandiseAtSite(site.getId());
        return new SiteRow(
            site,
            site.getId(),
            site.getSiteCode(),
            site.getName(),
            OrderingFormatters.blankToFallback(site.getDescription(), "-"),
            OrderingFormatters.formatDays(site.getShipDeliveryDays()),
            OrderingFormatters.formatDays(site.getAirDeliveryDays()),
            String.valueOf(itemCount)
        );
    }

    public SiteManagementResult createSite(SiteDraft draft) {
        if (draft.siteCode() == null || draft.siteCode().isBlank()) {
            return SiteManagementResult.failure("Mã site không được để trống.");
        }
        if (draft.name() == null || draft.name().isBlank()) {
            return SiteManagementResult.failure("Tên site không được để trống.");
        }
        if (draft.shipDeliveryDays() != null && draft.shipDeliveryDays() < 0) {
            return SiteManagementResult.failure("Ngày vận chuyển đường biển không hợp lệ.");
        }
        if (draft.airDeliveryDays() != null && draft.airDeliveryDays() < 0) {
            return SiteManagementResult.failure("Ngày vận chuyển đường hàng không không hợp lệ.");
        }
        if (siteCommandRepository.existsBySiteCode(draft.siteCode())) {
            return SiteManagementResult.failure("Mã site đã tồn tại.");
        }
        int siteId = siteCommandRepository.createSite(draft);
        return SiteManagementResult.success("Tạo site thành công.", siteId);
    }

    public SiteManagementResult updateSite(int siteId, SiteDraft draft) {
        if (draft.siteCode() == null || draft.siteCode().isBlank()) {
            return SiteManagementResult.failure("Mã site không được để trống.");
        }
        if (draft.name() == null || draft.name().isBlank()) {
            return SiteManagementResult.failure("Tên site không được để trống.");
        }
        if (draft.shipDeliveryDays() != null && draft.shipDeliveryDays() < 0) {
            return SiteManagementResult.failure("Ngày vận chuyển đường biển không hợp lệ.");
        }
        if (draft.airDeliveryDays() != null && draft.airDeliveryDays() < 0) {
            return SiteManagementResult.failure("Ngày vận chuyển đường hàng không không hợp lệ.");
        }
        if (siteCommandRepository.existsBySiteCodeExceptId(draft.siteCode(), siteId)) {
            return SiteManagementResult.failure("Mã site đã tồn tại.");
        }
        siteCommandRepository.updateSite(siteId, draft);
        return SiteManagementResult.success("Cập nhật site thành công.", siteId);
    }

    public SiteManagementResult provisionSiteAccount(int siteId, SiteAccountDraft draft) {
        Site site = siteService.findById(siteId);
        if (site == null) {
            return SiteManagementResult.failure("Site không tồn tại.");
        }
        if (siteAccountProvisioningPort.usernameExists(draft.username())) {
            return SiteManagementResult.failure("Tên đăng nhập đã tồn tại.");
        }
        int[] createdAccountId = { 0 };
        try {
            transactionRunner.execute(() -> {
                createdAccountId[0] = siteAccountProvisioningPort.createSiteAccount(draft, siteId);
            });
        } catch (TransactionException e) {
            return SiteManagementResult.failure("Lỗi tạo tài khoản site: " + e.getMessage());
        }
        return SiteManagementResult.success("Tạo tài khoản site thành công.", createdAccountId[0]);
    }

    public record Snapshot(
        List<SiteRow> rows,
        int totalSites,
        int activeSites,
        int merchandiseCount
    ) {
        public Snapshot {
            rows = rows == null ? List.of() : List.copyOf(rows);
        }
    }
}
