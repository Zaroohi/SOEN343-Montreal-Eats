
package com.bakedtogo.business.billing;

import java.time.Instant;
import com.bakedtogo.business.domain.enums.OrderStatus;

public record OrderHistoryResponse(
    String orderId,
    OrderStatus finalState,
    Instant createdAt,
    Instant deliveredAt,
    Double totalCharge
) {}
