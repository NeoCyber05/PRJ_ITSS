package org.itss.prj_itss.entity;

public class SiteInventory {

    private int siteId;
    private int merchandiseId;
    private int stockQuantity;

    public SiteInventory() {
    }

    public SiteInventory(int siteId, int merchandiseId, int stockQuantity) {
        this.siteId = siteId;
        this.merchandiseId = merchandiseId;
        this.stockQuantity = stockQuantity;
    }


    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(int merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return "SiteInventory{" +
                "siteId=" + siteId +
                ", merchandiseId=" + merchandiseId +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
