package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.Role;

public class Courier extends User {

    public Courier(String id, String name, String password) {
        super(id, name, password, Role.COURIER);
    }
}

