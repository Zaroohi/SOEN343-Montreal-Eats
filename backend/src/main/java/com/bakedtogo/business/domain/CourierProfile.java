package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.exception.BusinessRuleException;

/** Delivery-side state of a courier (DLV). Holds the transition rules of R-DLV-03. */
public class CourierProfile {
    private final String id;
    private volatile double latitude;
    private volatile double longitude;
    private volatile CourierStatus status;
    private volatile String currentOrderId;   // set once the courier accepts
    private volatile String pendingOfferId;   // order offered but not yet accepted
    private volatile long legStartedAt;       // epoch millis when the current trip started, 0 = not travelling

    public CourierProfile(String id, double latitude, double longitude, CourierStatus status) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public String getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public CourierStatus getStatus() { return status; }
    public String getCurrentOrderId() { return currentOrderId; }
    public String getPendingOfferId() { return pendingOfferId; }

    /** Idle, on duty, and holding no offer: eligible for dispatch (R-DLV-02). */
    public boolean isAvailable() {
        return status == CourierStatus.IDLE && pendingOfferId == null;
    }

    /** Straight-line distance on the seeded grid. */
    public double distanceTo(double lat, double lon) {
        double dLat = latitude - lat;
        double dLon = longitude - lon;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    // ---- position (used by the city map) ----
    public void moveTo(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    /** How long the current trip has been going (0 when the courier is not travelling). */
    public long legElapsedMillis() {
        long start = legStartedAt;
        return start == 0 ? 0 : System.currentTimeMillis() - start;
    }

    // ---- UC-DLV-01 ----
    public void goOnDuty() {
        if (status != CourierStatus.OFFLINE) {
            throw new BusinessRuleException("Courier is already on duty (" + status + ")");
        }
        status = currentOrderId != null ? CourierStatus.ASSIGNED : CourierStatus.IDLE;
    }

    public void goOffDuty() {
        if (status == CourierStatus.OFFLINE) {
            throw new BusinessRuleException("Courier is already offline");
        }
        if (status == CourierStatus.CARRYING) {
            throw new BusinessRuleException("Cannot go offline while carrying an order");
        }
        status = CourierStatus.OFFLINE;   // an ASSIGNED order stays attached, so it can be stalled (UC-DLV-07)
        legStartedAt = 0;
    }

    // ---- offers (UC-DLV-02, 03) ----
    public void offer(String orderId) {
        if (!isAvailable()) {
            throw new BusinessRuleException("Courier " + id + " cannot receive an offer now");
        }
        pendingOfferId = orderId;
    }

    public void clearOffer() { pendingOfferId = null; }

    public void accept() {
        if (status != CourierStatus.IDLE || pendingOfferId == null) {
            throw new BusinessRuleException("No pending offer to accept");
        }
        currentOrderId = pendingOfferId;
        pendingOfferId = null;
        status = CourierStatus.ASSIGNED;
        legStartedAt = System.currentTimeMillis();     // starts driving to the bakery
    }

    // ---- UC-DLV-05, 06 ----
    public void pickUp() {
        if (status != CourierStatus.ASSIGNED) {
            throw new BusinessRuleException("Courier must be assigned to pick up (" + status + ")");
        }
        status = CourierStatus.CARRYING;
        legStartedAt = System.currentTimeMillis();     // starts driving to the customer
    }

    public void deliver() {
        if (status != CourierStatus.CARRYING) {
            throw new BusinessRuleException("Courier is not carrying an order (" + status + ")");
        }
        currentOrderId = null;
        status = CourierStatus.IDLE;
        legStartedAt = 0;
    }

    // ---- UC-DLV-07 ----
    public void release() {
        if (status == CourierStatus.CARRYING) {
            throw new BusinessRuleException("Cannot release an order that is already picked up");
        }
        currentOrderId = null;
        legStartedAt = 0;
        if (status == CourierStatus.ASSIGNED) status = CourierStatus.OFFLINE;
    }
}