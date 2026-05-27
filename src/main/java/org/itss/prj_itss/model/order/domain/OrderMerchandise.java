package org.itss.prj_itss.model.order.domain;
import java.math.BigDecimal;

public class OrderMerchandise {

    private int orderId;
    private int merchandiseId;
    private BigDecimal quantity;
    private String deliveryMethod;

    public OrderMerchandise() {
    }

    public OrderMerchandise(int orderId, int merchandiseId,
                            BigDecimal quantity, String deliveryMethod) {
        this.orderId = orderId;
        this.merchandiseId = merchandiseId;
        this.quantity = quantity;
        this.deliveryMethod = deliveryMethod;
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(int merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    @Override
    public String toString() {
        return "OrderMerchandise{" +
                "orderId=" + orderId +
                ", merchandiseId=" + merchandiseId +
                ", quantity=" + quantity +
                ", deliveryMethod='" + deliveryMethod + '\'' +
                '}';
    }
}
