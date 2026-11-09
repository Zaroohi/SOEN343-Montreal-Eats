package com.bakedtogo.business.billing;

import com.bakedtogo.business.domain.Order;

/** Seam to PRC: told when an order is delivered so the fare can be computed and ledgered. */
public interface FareNotifier {
    void orderDelivered(Order order);
}