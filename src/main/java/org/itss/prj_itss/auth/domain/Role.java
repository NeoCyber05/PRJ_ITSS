package org.itss.prj_itss.auth.domain;

public record Role(int id, String name) {

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
