package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class PickedUpState implements OrderState {
    public OrderStatus status() { return OrderStatus.PICKED_UP; }
    @Override public OrderState deliver() { return new DeliveredState(); }
}