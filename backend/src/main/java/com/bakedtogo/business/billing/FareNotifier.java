
package com.bakedtogo.business.billing;

import com.bakedtogo.business.domain.Order;

/** Notifies PRC when an order is delivered. */
public interface FareNotifier {
    void orderDelivered(Order order, String courierId);
}
