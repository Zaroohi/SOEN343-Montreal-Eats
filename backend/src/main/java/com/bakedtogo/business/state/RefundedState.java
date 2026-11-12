
package com.bakedtogo.business.state;

import com.bakedtogo.business.domain.enums.OrderStatus;
// Order that has been successfully refunded

public class RefundedState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.REFUNDED;
    }
}
