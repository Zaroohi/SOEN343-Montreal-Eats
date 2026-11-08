package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class PreparingState implements OrderState {
    public OrderStatus status() { return OrderStatus.PREPARING; }
    @Override public OrderState markReady() { return new ReadyState(); }
}