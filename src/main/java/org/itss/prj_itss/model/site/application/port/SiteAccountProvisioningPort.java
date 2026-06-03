package org.itss.prj_itss.model.site.application.port;

import org.itss.prj_itss.model.site.application.SiteAccountDraft;

public interface SiteAccountProvisioningPort {
    boolean usernameExists(String username);
    int createSiteAccount(SiteAccountDraft draft, int siteId);
}
