package com.bakedtogo.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;

class EventLoggerTest {

    @Test
    void isASingleton() {
        assertSame(EventLogger.getInstance(), EventLogger.getInstance());
    }

    @Test
    void everyEventGetsAUniqueId() {
        EventLogger log = EventLogger.getInstance();
        OrderEvent a = log.record("o1", null, OrderStatus.PLACED, "c1");
        OrderEvent b = log.record("o1", OrderStatus.PLACED, OrderStatus.ACCEPTED, "s1");
        assertNotEquals(a.eventId(), b.eventId());
    }
}