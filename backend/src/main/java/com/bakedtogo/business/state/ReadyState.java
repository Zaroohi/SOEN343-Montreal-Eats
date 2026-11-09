package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class ReadyState implements OrderState {
    public OrderStatus status() { return OrderStatus.READY; }
    @Override public OrderState pickUp() { return new PickedUpState(); }
}