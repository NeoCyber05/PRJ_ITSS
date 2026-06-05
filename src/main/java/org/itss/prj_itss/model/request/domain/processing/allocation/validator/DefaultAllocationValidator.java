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
                errors.add("- " + item.code + " chỉ phân bổ " + allocated + "/" + item.required);
            }
            if (allocated > item.required) {
                errors.add("- " + item.code + " phân bổ vượt " + allocated + "/" + item.required);
            }
        }
        return errors;
    }

    @Override
    public String validateSubmission(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates
    ) {
        AllocationPlan plan = AllocationPlan.using(allocations);
        for (ItemRequirement item : items) {
            int allocated = plan.allocatedQuantity(item.merchandiseId);
            if (allocated < item.required) {
                return "Chưa đủ số lượng hàng cần";
            }
            if (allocated > item.required) {
                return "Số lượng phân bổ vượt yêu cầu";
            }
        }

        for (ItemRequirement item : items) {
            Map<Integer, Allocation> itemAllocations = allocations.getOrDefault(item.merchandiseId, Map.of());
            if (itemAllocations.isEmpty()) {
                continue;
            }

            LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
            if (desiredDate == null) {
                continue;
            }

            int itemDeadlineDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), desiredDate);
            for (Allocation allocation : itemAllocations.values()) {
                SiteStockOption site = allSites.stream()
                    .filter(candidate -> candidate.id == allocation.siteId)
                    .findFirst()
                    .orElse(null);
                if (site == null) {
                    return "Không đáp ứng ngày nhận mong muốn";
                }

                Integer deliveryDays = DeliveryOptions.deliveryDays(
                    site,
                    DeliveryOptions.resolve(site, allocation.transport, itemDeadlineDays)
                );
                if (deliveryDays == null || deliveryDays > itemDeadlineDays) {
                    return "Không đáp ứng ngày nhận mong muốn";
                }
            }
        }

        return null;
    }
}
