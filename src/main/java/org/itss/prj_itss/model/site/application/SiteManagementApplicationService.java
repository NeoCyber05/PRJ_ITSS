package org.itss.prj_itss.model.site.application;

import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.common.application.OrderingFormatters;
import org.itss.prj_itss.model.site.application.SiteRow;
import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.site.application.SiteUseCase;

import java.util.List;
import java.util.Objects;

public final class SiteManagementApplicationService {

    private final SiteUseCase siteService;
    private final CatalogUseCase merchandiseService;

    public SiteManagementApplicationService(SiteUseCase siteService, CatalogUseCase merchandiseService) {
        this.siteService = Objects.requireNonNull(siteService, "siteService");
        this.merchandiseService = Objects.requireNonNull(merchandiseService, "merchandiseService");
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
