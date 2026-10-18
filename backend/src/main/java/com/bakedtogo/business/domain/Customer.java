package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.Role;

public class Customer extends User {

    public Customer(String id, String name, String password) {
        super(id, name, password, Role.CUSTOMER);
    }
}
