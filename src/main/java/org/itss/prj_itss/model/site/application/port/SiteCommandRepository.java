package org.itss.prj_itss.model.site.application.port;

import org.itss.prj_itss.model.site.application.SiteDraft;

public interface SiteCommandRepository {
    int createSite(SiteDraft draft);
    void updateSite(int siteId, SiteDraft draft);
    boolean existsBySiteCode(String siteCode);
    boolean existsBySiteCodeExceptId(String siteCode, int siteId);
}
