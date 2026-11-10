
package com.bakedtogo.business.billing;

import com.bakedtogo.business.pricing.PricingBreakdown;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FareLedger {

    private final Map<String, PricingBreakdown> entries =
            new ConcurrentHashMap<>();

    public void record(String orderId, PricingBreakdown breakdown) {
        if (orderId == null || breakdown == null) {
            throw new IllegalArgumentException(
                    "Order ID and pricing breakdown are required"
            );
        }

        entries.putIfAbsent(orderId, breakdown);
    }

    public Optional<PricingBreakdown> findByOrderId(String orderId) {
        return Optional.ofNullable(entries.get(orderId));
    }
}
