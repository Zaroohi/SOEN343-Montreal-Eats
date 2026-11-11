package com.bakedtogo.business.billing;

import java.math.BigDecimal;
import java.time.Instant;


// Concrete creator in the Factory Method 
public class ChargeEntryFactory extends LedgerEntryFactory {

    @Override
    public LedgerTransaction createEntry(
        String orderId,
        BigDecimal amount
    ) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                "Charge amount must be non-negative"
            );
        }

        return new LedgerTransaction(
            orderId,
            "CHARGE",
            amount,
            Instant.now()
        );
    }
}
