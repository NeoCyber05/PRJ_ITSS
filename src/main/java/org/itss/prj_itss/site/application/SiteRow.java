package org.itss.prj_itss.site.application;

import org.itss.prj_itss.site.domain.Site;

import java.util.Locale;

public record SiteRow(
    Site site,
    int siteId,
    String siteCode,
    String siteName,
    String description,
    String shipDays,
    String airDays,
    String itemCount
) {

    public boolean matchesKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
            || contains(siteCode, normalized)
            || contains(siteName, normalized)
            || contains(description, normalized);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
