package org.itss.prj_itss.model.site.application.port;

import org.itss.prj_itss.model.site.application.self.SiteProfileDraft;

public interface SiteProfileCommandPort {
    void updateProfile(int siteId, SiteProfileDraft draft);
}
