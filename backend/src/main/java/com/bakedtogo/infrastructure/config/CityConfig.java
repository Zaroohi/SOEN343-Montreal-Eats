package com.bakedtogo.infrastructure.config;

import java.util.List;

public record CityConfig(List<BakeryConfig> bakeries,
                         List<CourierConfig> couriers,
                         List<CustomerConfig> customers) {

    public record BakeryConfig(String id, String name, String status,
                               double latitude, double longitude,
                               List<MenuItemConfig> menu) {}
    public record MenuItemConfig(String id, String name, double price, String status) {}
    public record CourierConfig(String id, double latitude, double longitude, String status) {}
    public record CustomerConfig(String id, String name, double latitude, double longitude) {}
}