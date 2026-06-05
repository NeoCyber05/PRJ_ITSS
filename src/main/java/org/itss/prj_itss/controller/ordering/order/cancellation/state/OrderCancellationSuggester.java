package org.itss.prj_itss.controller.ordering.order.cancellation.state;

import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OrderCancellationSuggester {

    public Map<Integer, Map<Integer, AllocationDraft>> suggest(
        int optionId,
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds
    ) {
        Map<Integer, Map<Integer, AllocationDraft>> results = new LinkedHashMap<>();

        for (ItemRequirement item : items) {
            Map<Integer, AllocationDraft> itemDrafts = new LinkedHashMap<>();
            int remaining = item.required;

            // Get candidate sites (exclude the cancelled site and only sites with stock > 0 for this merchandise)
            List<SiteStockOption> candidates = new ArrayList<>();
            for (SiteStockOption site : allSites) {
                if (excludedSiteIds.contains(site.id)) {
                    continue;
                }
                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                if (stock > 0) {
                    candidates.add(site);
                }
            }

            // Sort candidates by stock DESC (Greedy)
            candidates.sort((a, b) -> Integer.compare(
                b.stock.getOrDefault(item.merchandiseId, 0),
                a.stock.getOrDefault(item.merchandiseId, 0)
            ));

            // Greedy fill
            for (SiteStockOption site : candidates) {
                if (remaining <= 0) {
                    break;
                }
                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int take = Math.min(stock, remaining);
                if (take > 0) {
                    String transport = determineTransport(optionId, item, site, take);
                    itemDrafts.put(site.id, new AllocationDraft(site.id, item.merchandiseId, take, transport));
                    remaining -= take;
                }
            }

            results.put(item.merchandiseId, itemDrafts);
        }

        return results;
    }

    private String determineTransport(int optionId, ItemRequirement item, SiteStockOption site, int quantity) {
        if (optionId == 1) {
            // Option 1: Chi phí vận chuyển thấp -> Đường tàu hoàn toàn (nếu khả dụng), fallback hàng không
            if (site.shipDays < 999) {
                return "ship";
            }
            if (site.airDays < 999) {
                return "air";
            }
            return "ship";
        } else if (optionId == 2) {
            // Option 2: Quản lý đơn giản -> Hàng không hoàn toàn (nếu khả dụng), fallback tàu
            if (site.airDays < 999) {
                return "air";
            }
            if (site.shipDays < 999) {
                return "ship";
            }
            return "air";
        } else {
            // Option 3: Cân bằng -> Lô hàng lớn (>= 50% nhu cầu) dùng tàu, lô nhỏ dùng máy bay
            double proportion = item.required > 0 ? (double) quantity / item.required : 0.0;
            if (proportion >= 0.5) {
                if (site.shipDays < 999) {
                    return "ship";
                }
                return site.airDays < 999 ? "air" : "ship";
            } else {
                if (site.airDays < 999) {
                    return "air";
                }
                return site.shipDays < 999 ? "ship" : "air";
            }
        }
    }
}
