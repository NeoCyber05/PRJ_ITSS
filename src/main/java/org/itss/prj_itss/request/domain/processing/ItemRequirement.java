package org.itss.prj_itss.request.domain.processing;

public final class ItemRequirement {
    public final int merchandiseId;
    public final String code;
    public final String name;
    public final int required;

    public ItemRequirement(int merchandiseId, String code, String name, int required) {
        this.merchandiseId = merchandiseId;
        this.code = code;
        this.name = name;
        this.required = required;
    }
}

