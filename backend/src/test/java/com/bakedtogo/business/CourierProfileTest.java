package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

class CourierProfileTest {
    private CourierProfile idle() {
        CourierProfile c = new CourierProfile("d1", 45.5, -73.5, CourierStatus.OFFLINE);
        c.goOnDuty();
        return c;
    }

    @Test void goingOnDutyMakesIdle() {
        assertEquals(CourierStatus.IDLE, idle().getStatus());
    }

    @Test void fullDeliveryCycle() {
        CourierProfile c = idle();
        c.offer("ORD-1");
        c.accept();
        assertEquals(CourierStatus.ASSIGNED, c.getStatus());
        c.pickUp();
        assertEquals(CourierStatus.CARRYING, c.getStatus());
        c.deliver();
        assertEquals(CourierStatus.IDLE, c.getStatus());
        assertNull(c.getCurrentOrderId());
    }

    @Test void cannotGoOfflineWhileCarrying() {
        CourierProfile c = idle();
        c.offer("ORD-1"); c.accept(); c.pickUp();
        assertThrows(BusinessRuleException.class, c::goOffDuty);
    }

    @Test void cannotAcceptWithoutOffer() {
        assertThrows(BusinessRuleException.class, () -> idle().accept());
    }

    @Test void courierWithPendingOfferIsNotAvailable() {
        CourierProfile c = idle();
        c.offer("ORD-1");
        assertFalse(c.isAvailable());
    }

    @Test void offlineCourierCannotBeOffered() {
        CourierProfile c = new CourierProfile("d1", 0, 0, CourierStatus.OFFLINE);
        assertThrows(BusinessRuleException.class, () -> c.offer("ORD-1"));
    }
}