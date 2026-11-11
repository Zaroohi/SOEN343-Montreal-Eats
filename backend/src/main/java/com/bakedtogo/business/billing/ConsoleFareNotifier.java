
package com.bakedtogo.business.billing;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.pricing.PricingBreakdown;
import com.bakedtogo.business.pricing.PricingStrategy;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ConsoleFareNotifier implements FareNotifier {

    private final PricingStrategy pricing;
    private final FareLedger ledger;

    public ConsoleFareNotifier(PricingStrategy pricing, FareLedger ledger) {
        this.pricing = pricing;
        this.ledger = ledger;
    }

    @Override
public void orderDelivered(Order order, String courierId) {

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                "Cannot record fare before order is delivered"
            );
        }

        // PRC-01: Calculate the final fare
        PricingBreakdown breakdown =
                pricing.calculateBreakdown(order.getLines());

        order.setPricingBreakdown(breakdown);

        // PRC-03: Create the ledger entry
        LedgerEntry entry = new LedgerEntry(
        order.getId(),
        order.getCustomerId(),
        order.getBakeryId(),
        courierId,
        order.getStatus(),
        order.getCreatedAt(),
        Instant.now(),
        breakdown.total()
        );

        // Store the ledger entry
        ledger.record(entry);

        // Log the recorded transaction
        System.out.println(
                "[PRC-03] Ledger entry recorded for order " + entry.orderId()  + " | Status: " + entry.finalState()  + " | Created: " + entry.createdAt()+ " | Delivered: " + entry.deliveredAt() + " | Total: $" + entry.totalCharge());
    }
}
