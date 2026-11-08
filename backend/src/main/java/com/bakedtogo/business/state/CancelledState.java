package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class CancelledState implements OrderState {
    public OrderStatus status() { return OrderStatus.CANCELLED; }
}