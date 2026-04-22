package org.itss.prj_itss.entity;

public class Site {

    private int id;
    private String siteCode;
    private String name;
    private String description;
    private Integer shipDeliveryDays;
    private Integer airDeliveryDays;

    public Site() {
    }

    public Site(int id, String siteCode, String name, String description,
                Integer shipDeliveryDays, Integer airDeliveryDays) {
        this.id = id;
        this.siteCode = siteCode;
        this.name = name;
        this.description = description;
        this.shipDeliveryDays = shipDeliveryDays;
        this.airDeliveryDays = airDeliveryDays;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getShipDeliveryDays() {
        return shipDeliveryDays;
    }

    public void setShipDeliveryDays(Integer shipDeliveryDays) {
        this.shipDeliveryDays = shipDeliveryDays;
    }

    public Integer getAirDeliveryDays() {
        return airDeliveryDays;
    }

    public void setAirDeliveryDays(Integer airDeliveryDays) {
        this.airDeliveryDays = airDeliveryDays;
    }

    @Override
    public String toString() {
        return "Site{" +
                "id=" + id +
                ", siteCode='" + siteCode + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", shipDeliveryDays=" + shipDeliveryDays +
                ", airDeliveryDays=" + airDeliveryDays +
                '}';
    }
}
