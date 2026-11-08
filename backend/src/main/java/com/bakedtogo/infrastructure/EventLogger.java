// infrastructure/EventLogger.java
package com.bakedtogo.infrastructure;

import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.domain.enums.OrderStatus;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public final class EventLogger {

    private static final EventLogger INSTANCE = new EventLogger();
    private final AtomicLong counter = new AtomicLong();

    private EventLogger() {}

    public static EventLogger getInstance() { return INSTANCE; }

    // Creates an event with a unique id and logs it to the console.
    public OrderEvent record(String orderId, OrderStatus from, OrderStatus to, String actorId) {
        String eventId = "EVT-" + String.format("%04d", counter.incrementAndGet());
        OrderEvent event = new OrderEvent(eventId, orderId, from, to, Instant.now(), actorId);
        System.out.println("[EVENT] " + eventId + " order=" + orderId + " "
                + (from == null ? "(new)" : from) + " -> " + to + " by=" + actorId);
        return event;
    }

    // For tests: restores a known starting point.
    public void reset() { counter.set(0); }
}