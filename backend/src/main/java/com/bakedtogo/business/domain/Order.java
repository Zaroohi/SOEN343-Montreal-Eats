package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.state.OrderState;
import com.bakedtogo.business.state.PlacedState;
import java.time.Instant;
import java.util.List;

public class Order {
    private final String id;
    private final String customerId;
    private final String bakeryId;
    private final List<OrderLine> lines;
    private final Instant createdAt = Instant.now();
    private OrderState state = new PlacedState();
    private String rejectionReason;
    private double total;

    public Order(String id, String customerId, String bakeryId, List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessRuleException("An order needs at least one item");
        }
        this.id = id;
        this.customerId = customerId;
        this.bakeryId = bakeryId;
        this.lines = List.copyOf(lines);
    }

    public void accept()         { state = state.accept(); }
    public void startPreparing() { state = state.startPreparing(); }
    public void markReady()      { state = state.markReady(); }
    public void cancel()         { state = state.cancel(); }
    public void pickUp()         { state = state.pickUp(); }
    public void deliver()        { state = state.deliver(); }

    public void reject(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("A rejection needs a reason");
        }
        state = state.reject();         
        this.rejectionReason = reason;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getBakeryId() { return bakeryId; }
    public List<OrderLine> getLines() { return lines; }
    public Instant getCreatedAt() { return createdAt; }
    public String getRejectionReason() { return rejectionReason; }
    public OrderStatus getStatus() { return state.status(); }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}