package org.itss.prj_itss.auth.domain;

public record Account(
    int id,
    String username,
    String password,
    String fullName,
    String status,
    int roleId,
    Integer siteId
) {

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStatus() {
        return status;
    }

    public int getRoleId() {
        return roleId;
    }

    public Integer getSiteId() {
        return siteId;
    }
}
