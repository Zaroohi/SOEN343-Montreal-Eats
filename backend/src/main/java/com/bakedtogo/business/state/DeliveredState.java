package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;

public class DeliveredState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DELIVERED;
    }
    @Override
    public OrderState refund() {
        return new RefundedState();
    }

}