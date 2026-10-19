package com.bakedtogo.business.domain.enums;

public enum OrderStatus{
    PLACED, ACCEPTED, PREPARING, READY, REJECTED, CANCELLED;

    public boolean isTerminal(){
        return this == READY || this == REJECTED || this == CANCELLED;
    }
}