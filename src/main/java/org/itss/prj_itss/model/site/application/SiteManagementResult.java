package org.itss.prj_itss.model.site.application;

public record SiteManagementResult(boolean success, String message, Integer siteId) {

    public static SiteManagementResult success(String message, Integer siteId) {
        return new SiteManagementResult(true, message, siteId);
    }

    public static SiteManagementResult failure(String message) {
        return new SiteManagementResult(false, message, null);
    }
}
