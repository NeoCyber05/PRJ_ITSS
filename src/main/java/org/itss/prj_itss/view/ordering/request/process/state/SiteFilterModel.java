package org.itss.prj_itss.view.ordering.request.process.state;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class SiteFilterModel {

    private final List<ProcessingSiteView> visibleSites = new ArrayList<>();
    private final Set<Integer> selectedSiteIds = new LinkedHashSet<>();
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

    public Set<Integer> selectedSiteIds() {
        return selectedSiteIds;
    }

    public Set<Integer> excludedSiteIds() {
        return excludedSiteIds;
    }

    public void clearFilters() {
        selectedSiteIds.clear();
        excludedSiteIds.clear();
    }

    public void select(ProcessingSiteView site) {
        if (site == null) {
            return;
        }

        selectedSiteIds.add(site.id());
        excludedSiteIds.remove(site.id());
    }

    public void deselect(ProcessingSiteView site) {
        if (site != null) {
            selectedSiteIds.remove(site.id());
        }
    }

    public void exclude(ProcessingSiteView site) {
        if (site == null) {
            return;
        }

        excludedSiteIds.add(site.id());
        selectedSiteIds.remove(site.id());
    }

    public void removeSelected(int siteId) {
        selectedSiteIds.remove(siteId);
    }

    public void removeExcluded(int siteId) {
        excludedSiteIds.remove(siteId);
    }

    public boolean isSelected(ProcessingSiteView site) {
        return site != null && selectedSiteIds.contains(site.id());
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
        visibleSites.addAll(filteredSites);
    }

    public List<ProcessingSiteView> selectedSites() {
        return sitesFor(selectedSiteIds);
    }

    public List<ProcessingSiteView> excludedSites() {
        return sitesFor(excludedSiteIds);
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
