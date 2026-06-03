package org.itss.prj_itss.controller.site;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.itss.prj_itss.model.site.application.self.OverseasSiteApplicationService;
import org.itss.prj_itss.model.site.application.self.SiteInventoryDraft;
import org.itss.prj_itss.model.site.application.self.SiteOrderItemRow;
import org.itss.prj_itss.model.site.application.self.SiteProfileDraft;
import org.itss.prj_itss.model.site.application.self.SiteWorkspaceResult;
import org.itss.prj_itss.model.site.application.self.SiteWorkspaceSnapshot;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class SiteWorkspaceController {

    private final OverseasSiteApplicationService service;
    private final Supplier<AuthenticatedUser> authenticatedUserSupplier;

    public SiteWorkspaceController(
        OverseasSiteApplicationService service,
        Supplier<AuthenticatedUser> authenticatedUserSupplier
    ) {
        this.service = Objects.requireNonNull(service, "service");
        this.authenticatedUserSupplier = Objects.requireNonNull(authenticatedUserSupplier, "authenticatedUserSupplier");
    }

    public SiteWorkspaceSnapshot load() {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceSnapshot.unavailable("Tài khoản Site chưa được liên kết với site.");
        }
        return service.load(siteId);
    }

    public SiteWorkspaceResult updateProfile(SiteProfileDraft draft) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.updateProfile(siteId, draft);
    }

    public SiteWorkspaceResult updateInventoryItem(SiteInventoryDraft draft) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.updateInventoryItem(siteId, draft);
    }

    public SiteWorkspaceResult removeInventoryItem(int merchandiseId) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.removeInventoryItem(siteId, merchandiseId);
    }

    public SiteWorkspaceResult confirmSupply(int orderId) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.confirmSupply(siteId, orderId);
    }

    public List<SiteOrderItemRow> loadOrderItems(int orderId) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return List.of();
        }
        return service.loadOrderItems(siteId, orderId);
    }

    private Integer currentSiteId() {
        AuthenticatedUser user = authenticatedUserSupplier.get();
        if (user == null || !RoleType.from(user).isSiteRole()) {
            return null;
        }
        return user.account().getSiteId();
    }
}
