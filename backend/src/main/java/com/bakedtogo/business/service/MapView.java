package com.bakedtogo.business.service;

import java.util.List;

/** What the city map draws. Already filtered for the caller's role. */
public record MapView(List<BakeryPoint> bakeries, List<CourierPoint> couriers,
                      List<CustomerPoint> customers, List<String> dispatchQueue) {

    public record BakeryPoint(String id, String name, String status,
                              double lat, double lon, int queueLength) {}

    /** targetType is "offer", "pickup" or "delivery" (null when the courier has no active order). */
    public record CourierPoint(String id, String status, double lat, double lon,
                               String orderId, String targetType,
                               Double targetLat, Double targetLon, long legElapsedMillis) {}

    public record CustomerPoint(String id, String name, double lat, double lon) {}
}