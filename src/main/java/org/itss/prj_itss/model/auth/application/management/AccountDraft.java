package org.itss.prj_itss.model.auth.application.management;

public record AccountDraft(String username, String password, String fullName, int roleId, Integer siteId) {}
