package org.itss.prj_itss.model.request.domain.processing.allocation;

public final class Allocation {
    public final int siteId;
    public final int merchandiseId;
    private int quantity;
    public String transport;

    public Allocation(int siteId, int merchandiseId, int quantity, String transport) {
        this.siteId = siteId;
        this.merchandiseId = merchandiseId;
        this.quantity = quantity;
        this.transport = transport;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

