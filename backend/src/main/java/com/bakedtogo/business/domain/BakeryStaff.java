package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.Role;

public class BakeryStaff extends User {

    private String bakeryId;

    public BakeryStaff(String id, String name, String password, String bakeryId) {
        super(id, name, password, Role.BAKERY_STAFF);
        this.bakeryId = bakeryId;
    }

    public String getBakeryId() {
        return bakeryId;
    }
}
