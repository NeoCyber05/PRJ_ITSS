package org.itss.prj_itss.model.shared.formatting;

public final class DeliveryStatusFormatter {
    
    public record DeliveryStatusView(String text, String styleClass) {}
    
    private DeliveryStatusFormatter() {}
    
    public static DeliveryStatusView format(int dayDelta, boolean available) {
        if (!available) {
            return new DeliveryStatusView("Không khả dụng", "allocation-eta-unavailable");
        }
        if (dayDelta > 0) {
            return new DeliveryStatusView("Sớm " + dayDelta + " ngày", "allocation-eta-early");
        }
        if (dayDelta == 0) {
            return new DeliveryStatusView("Kịp hạn", "allocation-eta-on-time");
        }
        return new DeliveryStatusView("Trễ " + Math.abs(dayDelta) + " ngày", "allocation-eta-late");
    }
}
