package org.itss.prj_itss.request;

import javafx.beans.property.SimpleIntegerProperty;

import java.util.Map;

/**
 * Data models dùng riêng cho Request Processing flow.
 * Tách ra khỏi RequestProcessingView để giảm độ dài file.
 */
public final class RequestModels {

    private RequestModels() { }

    /** Mặt hàng cần đặt trong 1 request */
    public static class ItemReq {
        public final int merchandiseId;
        public final String code;
        public final String name;
        public final int required;

        public ItemReq(int merchandiseId, String code, String name, int required) {
            this.merchandiseId = merchandiseId;
            this.code = code;
            this.name = name;
            this.required = required;
        }
    }

    /** Thông tin site + tồn kho */
    public static class SiteInfo {
        public final int id;
        public final String siteCode;
        public final String name;
        public final String description;
        public final int shipDays;
        public final int airDays;
        public final Map<Integer, Integer> stock; // merchandiseId → qty

        public SiteInfo(int id, String siteCode, String name, String description,
                        int shipDays, int airDays, Map<Integer, Integer> stock) {
            this.id = id;
            this.siteCode = siteCode;
            this.name = name;
            this.description = description;
            this.shipDays = shipDays;
            this.airDays = airDays;
            this.stock = stock;
        }
    }

    /** Phân bổ: site × item → qty + transport */
    public static class Allocation {
        public final int siteId;
        public final int merchandiseId;
        public final SimpleIntegerProperty qty;
        public String transport; // "Tàu" | "Máy bay"

        public Allocation(int siteId, int merchandiseId, int qty, String transport) {
            this.siteId = siteId;
            this.merchandiseId = merchandiseId;
            this.qty = new SimpleIntegerProperty(qty);
            this.transport = transport;
        }
    }
}
