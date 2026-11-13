
package com.bakedtogo.business.billing;


import java.math.BigDecimal;

public abstract class LedgerEntryFactory {

    public abstract LedgerTransaction createEntry(
        String orderId,
        BigDecimal amount
    );
}
