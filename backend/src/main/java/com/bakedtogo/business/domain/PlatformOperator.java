package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.Role;

public class PlatformOperator extends User {

    public PlatformOperator(String id, String name, String password) {
        super(id, name, password, Role.OPERATOR);
    }
}
