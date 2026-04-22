package org.itss.prj_itss.entity;

public class Account {

    private int id;
    private String username;
    private String password;
    private String fullName;
    private String status;
    private int roleId;
    private Integer siteId; // nullable

    public Account() {
    }

    public Account(int id, String username, String password, String fullName,
                   String status, int roleId, Integer siteId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.status = status;
        this.roleId = roleId;
        this.siteId = siteId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", status='" + status + '\'' +
                ", roleId=" + roleId +
                ", siteId=" + siteId +
                '}';
    }
}
