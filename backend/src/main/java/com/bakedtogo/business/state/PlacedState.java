package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class PlacedState implements OrderState {
    public OrderStatus status() { return OrderStatus.PLACED; }
    @Override public OrderState accept() { return new AcceptedState(); }
    @Override public OrderState reject() { return new RejectedState(); }
    @Override public OrderState cancel() { return new CancelledState(); }
}