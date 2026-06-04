package org.itss.prj_itss.model.merchandise.domain;

public class Merchandise {

    private int id;
    private String code;
    private String name;
    private String unit;
    private boolean active;

    public Merchandise() {
    }

    public Merchandise(int id, String code, String name, String unit) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.active = true;
    }

    public Merchandise(int id, String code, String name, String unit, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Merchandise{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", active=" + active +
                '}';
    }
}
