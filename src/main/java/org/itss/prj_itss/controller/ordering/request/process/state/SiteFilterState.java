package org.itss.prj_itss.controller.ordering.request.process.state;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class SiteFilterState {

    private final List<ProcessingSiteState> visibleSites = new ArrayList<>();
    private final Set<Integer> selectedSiteIds = new LinkedHashSet<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    private List<ProcessingSiteState> allSites = List.of();

    public void setSites(List<ProcessingSiteState> allSites) {
        this.allSites = allSites == null ? List.of() : List.copyOf(allSites);
        visibleSites.clear();
        visibleSites.addAll(this.allSites);
    }

    public List<ProcessingSiteState> allSites() {
        return allSites;
    }

    public List<ProcessingSiteState> visibleSites() {
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

    public void select(ProcessingSiteState site) {
        if (site == null) {
            return;
        }

        selectedSiteIds.add(site.id());
        excludedSiteIds.remove(site.id());
    }

    public void deselect(ProcessingSiteState site) {
        if (site != null) {
            selectedSiteIds.remove(site.id());
        }
    }

    public void exclude(ProcessingSiteState site) {
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

    public boolean isSelected(ProcessingSiteState site) {
        return site != null && selectedSiteIds.contains(site.id());
    }

    public void refreshVisibleSites(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<ProcessingSiteState> filteredSites = new ArrayList<>();

        for (ProcessingSiteState site : allSites) {
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

    public List<ProcessingSiteState> selectedSites() {
        return sitesFor(selectedSiteIds);
    }

    public List<ProcessingSiteState> excludedSites() {
        return sitesFor(excludedSiteIds);
    }

    private List<ProcessingSiteState> sitesFor(Set<Integer> siteIds) {
        List<ProcessingSiteState> sites = new ArrayList<>();
        for (int siteId : siteIds) {
            findSite(allSites, siteId).ifPresent(sites::add);
        }
        return sites;
    }

    private static Optional<ProcessingSiteState> findSite(List<ProcessingSiteState> sites, int siteId) {
        return sites.stream()
            .filter(site -> site.id() == siteId)
            .findFirst();
    }

    private static boolean matches(ProcessingSiteState site, String keyword) {
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

