package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class AcceptedState implements OrderState {
    public OrderStatus status() { return OrderStatus.ACCEPTED; }
    @Override public OrderState startPreparing() { return new PreparingState(); }
}