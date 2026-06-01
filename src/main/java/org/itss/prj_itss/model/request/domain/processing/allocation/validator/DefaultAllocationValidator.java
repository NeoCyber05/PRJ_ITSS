package org.itss.prj_itss.model.request.domain.processing.allocation.validator;

import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationPlan;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryOptions;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultAllocationValidator implements AllocationValidator {
    @Override
    public List<String> validateAllocations(
        List<ItemRequirement> items,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        AllocationPlan plan = AllocationPlan.using(allocations);
        List<String> errors = new ArrayList<>();
        for (ItemRequirement item : items) {
            int allocated = plan.allocatedQuantity(item.merchandiseId);
            if (allocated < item.required) {
                errors.add("- " + item.code + " chi phan bo " + allocated + "/" + item.required);
            }
            if (allocated > item.required) {
                errors.add("- " + item.code + " phan bo vuot " + allocated + "/" + item.required);
            }
        }
        return errors;
    }

    @Override
    public String validateSubmission(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates,
        int deadlineDays
    ) {
        AllocationPlan plan = AllocationPlan.using(allocations);
        for (ItemRequirement item : items) {
            int allocated = plan.allocatedQuantity(item.merchandiseId);
            if (allocated < item.required) {
                return "Chua du so luong hang can";
            }
            if (allocated > item.required) {
                return "So luong phan bo vuot yeu cau";
            }
        }

        for (ItemRequirement item : items) {
            LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
            int itemDeadlineDays = desiredDate == null
                ? deadlineDays
                : Math.max(1, (int) ChronoUnit.DAYS.between(LocalDate.now(), desiredDate));

            Map<Integer, Allocation> itemAllocations = allocations.getOrDefault(item.merchandiseId, Map.of());
            for (Allocation allocation : itemAllocations.values()) {
                SiteStockOption site = allSites.stream()
                    .filter(candidate -> candidate.id == allocation.siteId)
                    .findFirst()
                    .orElse(null);
                if (site == null) {
                    return "Khong dap ung ngay nhan mong muon";
                }

                int deliveryDays = DeliveryOptions.deliveryDays(
                    site,
                    DeliveryOptions.resolve(site, allocation.transport, itemDeadlineDays)
                );
                if (deliveryDays >= 999 || deliveryDays > itemDeadlineDays) {
                    return "Khong dap ung ngay nhan mong muon";
                }
            }
        }

        return null;
    }
}
