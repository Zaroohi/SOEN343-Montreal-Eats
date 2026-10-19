package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.enums.Role;

public record Actor(String id, Role role, String bakeryId) {}