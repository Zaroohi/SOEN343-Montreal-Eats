package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.dispatch.*;
import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.enums.*;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.business.service.*;
import com.bakedtogo.persistence.memory.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispatchServiceTest {

    private static final double LAT = 45.5236, LON = -73.5817;   // bakery b1

    private InMemoryCourierRepository couriers;
    private CourierManager manager;
    private DispatchQueue queue;
    private DispatchService dispatch;
    private OrderService orders;
    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);
    private final Actor staff = new Actor("s1", Role.BAKERY_STAFF, "b1");
    private final List<OrderItemRequest> items = List.of(new OrderItemRequest("m1", 1));

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        Bakery b = new Bakery("b1", "Open", LAT, LON, BakeryStatus.OPEN);
        b.addMenuItem(new MenuItem("m1", "Croissant", 3.5, ItemStatus.AVAILABLE));
        bakeries.save(b);

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        couriers = new InMemoryCourierRepository();
        manager = new CourierManager();
        queue = new DispatchQueue();
        dispatch = new DispatchService(couriers, bakeries, orderRepo, queue, manager);
        orders = new OrderService(orderRepo, new InMemoryOrderEventRepository(), bakeries,
                new RegularPricing(), dispatch);
    }

    private CourierProfile courier(String id, double lat, double lon, CourierStatus status) {
        CourierProfile c = new CourierProfile(id, lat, lon, status);
        couriers.save(c);
        return c;
    }

    /** Runs a full bakery cycle; markReady triggers the dispatch. */
    private String readyOrder() {
        String id = orders.placeOrder(customer, "b1", items,null).order().getId();
        orders.acceptOrder(staff, id);
        orders.startPreparing(staff, id);
        orders.markReady(staff, id);
        return id;
    }

    @Test
    void nearestIdleCourierGetsTheOffer() {
        CourierProfile near = courier("d2", LAT + 0.001, LON, CourierStatus.IDLE);
        CourierProfile far = courier("d1", LAT + 0.05, LON, CourierStatus.IDLE);
        String id = readyOrder();
        assertEquals(id, near.getPendingOfferId());
        assertNull(far.getPendingOfferId());
    }

    @Test
    void tieIsBrokenByLowestCourierId() {
        CourierProfile d2 = courier("d2", LAT + 0.01, LON, CourierStatus.IDLE);
        CourierProfile d1 = courier("d1", LAT + 0.01, LON, CourierStatus.IDLE);
        String id = readyOrder();
        assertEquals(id, d1.getPendingOfferId());
        assertNull(d2.getPendingOfferId());
    }

    @Test
    void orderWaitsInQueueWhenNoCourierIsAvailable() {
        CourierProfile offline = courier("d1", LAT, LON, CourierStatus.OFFLINE);
        String id = readyOrder();
        assertEquals(List.of(id), queue.getPending());
        assertNull(offline.getPendingOfferId());
    }

    @Test
    void courierBecomingIdleReceivesTheWaitingOrder() {
        CourierProfile c = courier("d1", LAT, LON, CourierStatus.OFFLINE);
        String id = readyOrder();
        c.goOnDuty();
        manager.updateAvailability(c);
        assertEquals(id, c.getPendingOfferId());
        assertTrue(queue.getPending().isEmpty());
    }

    @Test
    void oldestWaitingOrderIsOfferedFirst() {
        CourierProfile c = courier("d1", LAT, LON, CourierStatus.OFFLINE);
        String first = readyOrder();
        String second = readyOrder();
        c.goOnDuty();
        manager.updateAvailability(c);
        assertEquals(first, c.getPendingOfferId());
        assertEquals(List.of(second), queue.getPending());
    }

    @Test
    void courierHoldingAnOfferIsNotOfferedAnotherOrder() {
        CourierProfile c = courier("d1", LAT, LON, CourierStatus.IDLE);
        String first = readyOrder();
        String second = readyOrder();
        assertEquals(first, c.getPendingOfferId());
        assertEquals(List.of(second), queue.getPending());
    }

    @Test
    void orderThatIsNotReadyCannotBeDispatched() {
        courier("d1", LAT, LON, CourierStatus.IDLE);
        String id = orders.placeOrder(customer, "b1", items,null).order().getId();
        assertThrows(BusinessRuleException.class, () -> dispatch.dispatch(id));
    }
}