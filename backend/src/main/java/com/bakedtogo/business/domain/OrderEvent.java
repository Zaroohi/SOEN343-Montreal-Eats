package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.OrderStatus;
import java.time.Instant;

public record OrderEvent(String eventId, String orderId,
                         OrderStatus fromStatus, OrderStatus toStatus,
                         Instant timestamp, String actorId) {}