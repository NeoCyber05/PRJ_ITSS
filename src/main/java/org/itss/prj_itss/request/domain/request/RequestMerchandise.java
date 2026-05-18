package org.itss.prj_itss.request.domain.request;

import java.math.BigDecimal;
import java.time.LocalDate;


public class RequestMerchandise {

    private int requestId;
    private int merchandiseId;
    private BigDecimal quantityOrdered;
    private LocalDate desiredDeliveryDate;

    public RequestMerchandise() {
    }

    public RequestMerchandise(int requestId, int merchandiseId,
                              BigDecimal quantityOrdered, LocalDate desiredDeliveryDate) {
        this.requestId = requestId;
        this.merchandiseId = merchandiseId;
        this.quantityOrdered = quantityOrdered;
        this.desiredDeliveryDate = desiredDeliveryDate;
    }


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(int merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public BigDecimal getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(BigDecimal quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public LocalDate getDesiredDeliveryDate() {
        return desiredDeliveryDate;
    }

    public void setDesiredDeliveryDate(LocalDate desiredDeliveryDate) {
        this.desiredDeliveryDate = desiredDeliveryDate;
    }

    @Override
    public String toString() {
        return "RequestMerchandise{" +
                "requestId=" + requestId +
                ", merchandiseId=" + merchandiseId +
                ", quantityOrdered=" + quantityOrdered +
                ", desiredDeliveryDate=" + desiredDeliveryDate +
                '}';
    }
}
