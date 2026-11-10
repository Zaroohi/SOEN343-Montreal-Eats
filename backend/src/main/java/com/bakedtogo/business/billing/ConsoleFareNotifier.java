
package com.bakedtogo.business.billing;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.pricing.PricingBreakdown;
import com.bakedtogo.business.pricing.PricingStrategy;
import org.springframework.stereotype.Component;

@Component
public class ConsoleFareNotifier implements FareNotifier {

    private final PricingStrategy pricing;
    private final FareLedger ledger;

    public ConsoleFareNotifier(PricingStrategy pricing, FareLedger ledger) {
        this.pricing = pricing;
        this.ledger = ledger;
    }

    @Override
    public void orderDelivered(Order order) {
        PricingBreakdown breakdown =
                pricing.calculateBreakdown(order.getLines());

        // Store the finalized fare in the order for PRC-02
        order.setPricingBreakdown(breakdown);

        // Record the finalized fare in the ledger for PRC-01
        ledger.record(order.getId(), breakdown);

        System.out.println("[PRC] Final fare computed and ledgered for order "
                + order.getId() + ": $" + breakdown.total());
    }
}
