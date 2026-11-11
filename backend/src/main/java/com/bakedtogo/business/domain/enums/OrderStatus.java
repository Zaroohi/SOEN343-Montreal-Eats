package com.bakedtogo.business.domain.enums;

public enum OrderStatus {
    PLACED, ACCEPTED, PREPARING, READY, PICKED_UP, DELIVERED, REJECTED, CANCELLED, REFUNDED;

    public boolean isTerminal() {
        return this == DELIVERED || this == REJECTED || this == CANCELLED;
    }
}