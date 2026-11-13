
package com.bakedtogo.business.billing;

import java.math.BigDecimal;
import java.time.Instant;

public class RefundEntryFactory extends LedgerEntryFactory {

    @Override
    public LedgerTransaction createEntry(
        String orderId,
        BigDecimal amount
    ) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                "Refund amount must be non-negative"
            );
        }

        return new LedgerTransaction(
            orderId,
            "REFUND",
            amount.negate(),
            Instant.now()
        );
    }
}
