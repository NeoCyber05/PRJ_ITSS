package org.itss.prj_itss.model.site.application.self;

public record SiteWorkspaceResult(boolean success, String message) {
    public static SiteWorkspaceResult success(String message) {
        return new SiteWorkspaceResult(true, message);
    }

    public static SiteWorkspaceResult failure(String message) {
        return new SiteWorkspaceResult(false, message);
    }
}
