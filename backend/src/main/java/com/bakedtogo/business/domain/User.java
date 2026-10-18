package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.Role;

public class User {
    private String id;
    private String name;
    private String password;
    private Role role;

    public User(String id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
