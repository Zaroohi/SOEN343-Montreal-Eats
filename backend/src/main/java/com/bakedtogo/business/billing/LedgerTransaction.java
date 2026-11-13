
package com.bakedtogo.business.billing;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerTransaction(
    String orderId,
    String type,
    BigDecimal amount,
    Instant timestamp
) {}
