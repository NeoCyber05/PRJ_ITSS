package org.itss.prj_itss.controller.ordering.request.process.site;

import org.itss.prj_itss.model.request.application.processing.ProcessingSiteView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class SiteFilterModel {

    private final List<ProcessingSiteView> visibleSites = new ArrayList<>();
    private final Set<Integer> prioritySiteIds = new LinkedHashSet<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    private List<ProcessingSiteView> allSites = List.of();

    public void setSites(List<ProcessingSiteView> allSites) {
        this.allSites = allSites == null ? List.of() : List.copyOf(allSites);
        visibleSites.clear();
        visibleSites.addAll(this.allSites);
    }

    public List<ProcessingSiteView> allSites() {
        return allSites;
    }

    public List<ProcessingSiteView> visibleSites() {
        return visibleSites;
    }

    public Set<Integer> prioritySiteIds() {
        return prioritySiteIds;
    }

    public Set<Integer> excludedSiteIds() {
        return excludedSiteIds;
    }

    public void clearFilters() {
        prioritySiteIds.clear();
        excludedSiteIds.clear();
    }

    public void prioritize(ProcessingSiteView site) {
        if (site == null) {
            return;
        }

        prioritySiteIds.add(site.id());
        excludedSiteIds.remove(site.id());
    }

    public void unprioritize(ProcessingSiteView site) {
        if (site != null) {
            prioritySiteIds.remove(site.id());
        }
    }

    public void exclude(ProcessingSiteView site) {
        if (site == null) {
            return;
        }

        excludedSiteIds.add(site.id());
        prioritySiteIds.remove(site.id());
    }

    public void removePriority(int siteId) {
        prioritySiteIds.remove(siteId);
    }

    public void removeExcluded(int siteId) {
        excludedSiteIds.remove(siteId);
    }

    public boolean isPriority(ProcessingSiteView site) {
        return site != null && prioritySiteIds.contains(site.id());
    }

    public void refreshVisibleSites(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<ProcessingSiteView> filteredSites = new ArrayList<>();

        for (ProcessingSiteView site : allSites) {
            if (excludedSiteIds.contains(site.id())) {
                continue;
            }

            if (normalizedKeyword.isEmpty() || matches(site, normalizedKeyword)) {
                filteredSites.add(site);
            }
        }

        visibleSites.clear();
        visibleSites.addAll(sortedSites(filteredSites));
    }

    public List<ProcessingSiteView> prioritySites() {
        return sitesFor(prioritySiteIds);
    }

    public List<ProcessingSiteView> excludedSites() {
        return sitesFor(excludedSiteIds);
    }

    private List<ProcessingSiteView> sortedSites(List<ProcessingSiteView> filteredSites) {
        List<ProcessingSiteView> sortedSites = new ArrayList<>();

        for (int prioritySiteId : prioritySiteIds) {
            findSite(filteredSites, prioritySiteId).ifPresent(sortedSites::add);
        }

        for (ProcessingSiteView site : filteredSites) {
            if (!prioritySiteIds.contains(site.id())) {
                sortedSites.add(site);
            }
        }

        return sortedSites;
    }

    private List<ProcessingSiteView> sitesFor(Set<Integer> siteIds) {
        List<ProcessingSiteView> sites = new ArrayList<>();
        for (int siteId : siteIds) {
            findSite(allSites, siteId).ifPresent(sites::add);
        }
        return sites;
    }

    private static Optional<ProcessingSiteView> findSite(List<ProcessingSiteView> sites, int siteId) {
        return sites.stream()
            .filter(site -> site.id() == siteId)
            .findFirst();
    }

    private static boolean matches(ProcessingSiteView site, String keyword) {
        return normalizeText(site.name()).contains(keyword)
            || normalizeText(site.siteCode()).contains(keyword);
    }

    private static String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeText(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
