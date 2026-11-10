package com.bakedtogo.business.billing;

import com.bakedtogo.business.domain.Order;
import org.springframework.stereotype.Component;

/** Placeholder until PRC exists: logs the hand-off. */
@Component
public class ConsoleFareNotifier implements FareNotifier {
    @Override
    public void orderDelivered(Order order) {
        System.out.println("[PRC] Order " + order.getId() + " delivered, total "
                + order.getTotal() + " (fare and ledger handled by PRC)");
    }
}