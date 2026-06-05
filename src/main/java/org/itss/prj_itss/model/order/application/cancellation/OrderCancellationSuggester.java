package org.itss.prj_itss.model.order.application.cancellation;

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
        Set<Integer> excludedSiteIds,
        int deadlineDays
    ) {
        Map<Integer, Map<Integer, AllocationDraft>> results = new LinkedHashMap<>();

        for (ItemRequirement item : items) {
            Map<Integer, AllocationDraft> itemDrafts = new LinkedHashMap<>();
            int remaining = item.required;

            // Get candidate sites (exclude the cancelled site, require stock > 0, and require timely delivery)
            List<SiteStockOption> candidates = new ArrayList<>();
            for (SiteStockOption site : allSites) {
                if (excludedSiteIds.contains(site.id)) {
                    continue;
                }
                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                if (stock > 0) {
                    boolean canDeliverTimely = (site.shipDays != null && site.shipDays <= deadlineDays)
                        || (site.airDays != null && site.airDays <= deadlineDays);
                    if (canDeliverTimely) {
                        candidates.add(site);
                    }
                }
            }

            // Determine sorting strategy based on option ID
            if (optionId == 1 || optionId == 2 || optionId == 3 || optionId == 5) {
                // Sort candidates by stock DESC (Greedy)
                candidates.sort((a, b) -> Integer.compare(
                    b.stock.getOrDefault(item.merchandiseId, 0),
                    a.stock.getOrDefault(item.merchandiseId, 0)
                ));
            } else if (optionId == 6) {
                // Option 6: Random allocation
                java.util.Collections.shuffle(candidates);
            } else {
                // Option 4: Follow priority in section 7 of usecase:
                // 1. Prefer ship transport capability over air-only (within deadline).
                // 2. Prefer larger stock.
                // 3. Keep original or ID ordering as tie-breaker.
                candidates.sort((a, b) -> {
                    boolean aHasShip = a.shipDays != null && a.shipDays <= deadlineDays;
                    boolean bHasShip = b.shipDays != null && b.shipDays <= deadlineDays;
                    if (aHasShip != bHasShip) {
                        return aHasShip ? -1 : 1;
                    }
                    int stockCompare = Integer.compare(
                        b.stock.getOrDefault(item.merchandiseId, 0),
                        a.stock.getOrDefault(item.merchandiseId, 0)
                    );
                    if (stockCompare != 0) {
                        return stockCompare;
                    }
                    return Integer.compare(a.id, b.id);
                });
            }

            // Greedy fill
            for (SiteStockOption site : candidates) {
                if (remaining <= 0) {
                    break;
                }
                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int take = Math.min(stock, remaining);
                if (take > 0) {
                    String transport = determineTransport(optionId, item, site, take, deadlineDays);
                    itemDrafts.put(site.id, new AllocationDraft(site.id, item.merchandiseId, take, transport));
                    remaining -= take;
                }
            }

            results.put(item.merchandiseId, itemDrafts);
        }

        return results;
    }

    private String determineTransport(int optionId, ItemRequirement item, SiteStockOption site, int quantity, int deadlineDays) {
        if (optionId == 1 || optionId == 4) {
            // Option 1 & 4: Tối ưu Chi phí (Đường tàu) -> Đường tàu hoàn toàn (nếu kịp deadline), fallback hàng không
            if (site.shipDays != null && site.shipDays <= deadlineDays) {
                return "ship";
            }
            if (site.airDays != null && site.airDays <= deadlineDays) {
                return "air";
            }
            return "ship";
        } else if (optionId == 2) {
            // Option 2: Giao nhanh (Hàng không) -> Hàng không hoàn toàn (nếu kịp deadline), fallback tàu
            if (site.airDays != null && site.airDays <= deadlineDays) {
                return "air";
            }
            if (site.shipDays != null && site.shipDays <= deadlineDays) {
                return "ship";
            }
            return "air";
        } else if (optionId == 6) {
            // Option 6: Phân bổ ngẫu nhiên -> Chọn ngẫu nhiên phương thức vận chuyển khả thi kịp ngày nhận
            List<String> valids = new ArrayList<>();
            if (site.shipDays != null && site.shipDays <= deadlineDays) {
                valids.add("ship");
            }
            if (site.airDays != null && site.airDays <= deadlineDays) {
                valids.add("air");
            }
            if (!valids.isEmpty()) {
                return valids.get(new java.util.Random().nextInt(valids.size()));
            }
            return "ship";
        } else {
            // Option 3 & 5: Cân bằng -> Lô hàng lớn (>= 50% nhu cầu) dùng tàu (nếu kịp), lô nhỏ dùng máy bay (nếu kịp)
            double proportion = item.required > 0 ? (double) quantity / item.required : 0.0;
            if (proportion >= 0.5) {
                if (site.shipDays != null && site.shipDays <= deadlineDays) {
                    return "ship";
                }
                if (site.airDays != null && site.airDays <= deadlineDays) {
                    return "air";
                }
                return "ship";
            } else {
                if (site.airDays != null && site.airDays <= deadlineDays) {
                    return "air";
                }
                if (site.shipDays != null && site.shipDays <= deadlineDays) {
                    return "ship";
                }
                return "air";
            }
        }
    }
}
