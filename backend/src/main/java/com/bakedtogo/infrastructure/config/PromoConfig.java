package com.bakedtogo.infrastructure.config;

import java.util.List;

public record PromoConfig(List<PromoEntry> promos) {
    public record PromoEntry(String code, String type, double value,
                             double minOrderValue, String expiresOn) {}
}