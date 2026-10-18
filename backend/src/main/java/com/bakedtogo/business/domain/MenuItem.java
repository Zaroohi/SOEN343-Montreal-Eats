package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.ItemStatus;

public class MenuItem {
    private final String id;
    private String name;
    private double price;
    private ItemStatus status;

    public MenuItem(String id, String name, double price, ItemStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public ItemStatus getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public boolean isAvailable() { return status == ItemStatus.AVAILABLE; }
}