package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.exception.IllegalTransitionException;

public interface OrderState {
    OrderStatus status();

    default OrderState accept()         { throw illegal(OrderStatus.ACCEPTED); }
    default OrderState reject()         { throw illegal(OrderStatus.REJECTED); }
    default OrderState startPreparing() { throw illegal(OrderStatus.PREPARING); }
    default OrderState markReady()      { throw illegal(OrderStatus.READY); }
    default OrderState cancel()         { throw illegal(OrderStatus.CANCELLED); }

    private IllegalTransitionException illegal(OrderStatus target) {
        return new IllegalTransitionException(
                "Cannot move order from " + status() + " to " + target);
    }
}