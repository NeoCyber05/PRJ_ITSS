package org.itss.prj_itss.order.domain;

import java.time.LocalDateTime;

public class Order {

    private int id;
    private int requestId;
    private int siteId;
    private LocalDateTime createdAt;
    private String status;

    public Order() {
    }

    public Order(int id, int requestId, int siteId, LocalDateTime createdAt, String status) {
        this.id = id;
        this.requestId = requestId;
        this.siteId = siteId;
        this.createdAt = createdAt;
        this.status = status;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", requestId=" + requestId +
                ", siteId=" + siteId +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }
}
