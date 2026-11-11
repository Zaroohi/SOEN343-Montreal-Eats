
package com.bakedtogo.business.billing;

import com.bakedtogo.business.domain.enums.OrderStatus;
import java.time.Instant;

public record LedgerEntry(
    String orderId,
    String customerId,
    String bakeryId,
    String courierId,
    OrderStatus finalState,
    Instant createdAt,
    Instant deliveredAt,
    double totalCharge
) {}
