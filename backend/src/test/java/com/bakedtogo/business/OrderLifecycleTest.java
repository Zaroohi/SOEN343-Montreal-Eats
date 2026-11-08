package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.IllegalTransitionException;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderLifecycleTest {

    private Order newOrder() {
        return new Order("o1", "c1", "b1", List.of(new OrderLine("m1", "Croissant", 3.5, 2)));
    }

    @Test
    void happyPathReachesReady() {
        Order o = newOrder();
        o.accept(); o.startPreparing(); o.markReady();
        assertEquals(OrderStatus.READY, o.getStatus());
    }

    @Test
    void cannotSkipFromPlacedToReady() {
        assertThrows(IllegalTransitionException.class, () -> newOrder().markReady());
    }

    @Test
    void cannotSkipFromAcceptedToReady() {
        Order o = newOrder();
        o.accept();
        assertThrows(IllegalTransitionException.class, o::markReady);
    }

    @Test
    void cannotCancelAfterAcceptance() {
        Order o = newOrder();
        o.accept();
        assertThrows(IllegalTransitionException.class, o::cancel);
    }

    @Test
    void rejectNeedsReason() {
        assertThrows(BusinessRuleException.class, () -> newOrder().reject(" "));
    }

    @Test
    void negativeQuantityRefused() {
        assertThrows(BusinessRuleException.class, () -> new OrderLine("m1", "Croissant", 3.5, -1));
    }
}