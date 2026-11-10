package com.bakedtogo.business.service;

/** What the courier panel needs: status plus the offered and current order with pickup bakery. */
public record CourierView(String courierId, String status,
                          String offeredOrderId, String offeredBakeryId,
                          String currentOrderId, String currentBakeryId) {}