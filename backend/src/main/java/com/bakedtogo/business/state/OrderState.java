package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.exception.IllegalTransitionException;

public interface OrderState {
    OrderStatus status();

    default OrderState accept() { throw illegal(OrderStatus.ACCEPTED); }
    default OrderState reject()  { throw illegal(OrderStatus.REJECTED); }
    default OrderState startPreparing() { throw illegal(OrderStatus.PREPARING); }
    default OrderState markReady() { throw illegal(OrderStatus.READY); }
    default OrderState cancel()  { throw illegal(OrderStatus.CANCELLED); }
    default OrderState pickUp() { throw illegal(OrderStatus.PICKED_UP); }
    default OrderState deliver()   { throw illegal(OrderStatus.DELIVERED); }
default OrderState refund() {
    throw illegal(OrderStatus.REFUNDED);
}
    private IllegalTransitionException illegal(OrderStatus target) {
        return new IllegalTransitionException(
                "Cannot move order from " + status() + " to " + target);
    }
}