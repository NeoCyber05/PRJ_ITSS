package org.itss.prj_itss.request.presentation.ordering.process.site;

import org.itss.prj_itss.request.business.model.SiteStockOption;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class SiteFilterModel {

    private final List<SiteStockOption> visibleSites = new ArrayList<>();
    private final Set<Integer> prioritySiteIds = new LinkedHashSet<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    private List<SiteStockOption> allSites = List.of();

    void setSites(List<SiteStockOption> allSites) {
        this.allSites = allSites == null ? List.of() : List.copyOf(allSites);
        visibleSites.clear();
        visibleSites.addAll(this.allSites);
    }

    List<SiteStockOption> allSites() {
        return allSites;
    }

    List<SiteStockOption> visibleSites() {
        return visibleSites;
    }

    Set<Integer> prioritySiteIds() {
        return prioritySiteIds;
    }

    Set<Integer> excludedSiteIds() {
        return excludedSiteIds;
    }

    void clearFilters() {
        prioritySiteIds.clear();
        excludedSiteIds.clear();
    }

    void prioritize(SiteStockOption site) {
        if (site == null) {
            return;
        }

        prioritySiteIds.add(site.id);
        excludedSiteIds.remove(site.id);
    }

    void unprioritize(SiteStockOption site) {
        if (site != null) {
            prioritySiteIds.remove(site.id);
        }
    }

    void exclude(SiteStockOption site) {
        if (site == null) {
            return;
        }

        excludedSiteIds.add(site.id);
        prioritySiteIds.remove(site.id);
    }

    void removePriority(int siteId) {
        prioritySiteIds.remove(siteId);
    }

    void removeExcluded(int siteId) {
        excludedSiteIds.remove(siteId);
    }

    boolean isPriority(SiteStockOption site) {
        return site != null && prioritySiteIds.contains(site.id);
    }

    void refreshVisibleSites(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<SiteStockOption> filteredSites = new ArrayList<>();

        for (SiteStockOption site : allSites) {
            if (excludedSiteIds.contains(site.id)) {
                continue;
            }

            if (normalizedKeyword.isEmpty() || matches(site, normalizedKeyword)) {
                filteredSites.add(site);
            }
        }

        visibleSites.clear();
        visibleSites.addAll(sortedSites(filteredSites));
    }

    List<SiteStockOption> prioritySites() {
        return sitesFor(prioritySiteIds);
    }

    List<SiteStockOption> excludedSites() {
        return sitesFor(excludedSiteIds);
    }

    private List<SiteStockOption> sortedSites(List<SiteStockOption> filteredSites) {
        List<SiteStockOption> sortedSites = new ArrayList<>();

        for (int prioritySiteId : prioritySiteIds) {
            findSite(filteredSites, prioritySiteId).ifPresent(sortedSites::add);
        }

        for (SiteStockOption site : filteredSites) {
            if (!prioritySiteIds.contains(site.id)) {
                sortedSites.add(site);
            }
        }

        return sortedSites;
    }

    private List<SiteStockOption> sitesFor(Set<Integer> siteIds) {
        List<SiteStockOption> sites = new ArrayList<>();
        for (int siteId : siteIds) {
            findSite(allSites, siteId).ifPresent(sites::add);
        }
        return sites;
    }

    private static Optional<SiteStockOption> findSite(List<SiteStockOption> sites, int siteId) {
        return sites.stream()
            .filter(site -> site.id == siteId)
            .findFirst();
    }

    private static boolean matches(SiteStockOption site, String keyword) {
        return normalizeText(site.name).contains(keyword)
            || normalizeText(site.siteCode).contains(keyword);
    }

    private static String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeText(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}

