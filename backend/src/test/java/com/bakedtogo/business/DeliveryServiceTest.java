package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.dispatch.*;
import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.enums.*;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.business.service.*;
import com.bakedtogo.persistence.memory.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bakedtogo.business.domain.CustomerLocation;
import com.bakedtogo.persistence.CustomerLocationRepository;

class DeliveryServiceTest {

    private static final double LAT = 45.5236, LON = -73.5817;

    private InMemoryCourierRepository couriers;
    private CustomerLocationRepository customers;
    private DispatchQueue queue;
    private OrderService orders;
    private DeliveryService delivery;
    private final AtomicInteger fareCalls = new AtomicInteger();

    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);
    private final Actor staff = new Actor("s1", Role.BAKERY_STAFF, "b1");
    private final Actor operator = new Actor("o1", Role.OPERATOR, null);
    private final Actor d1 = new Actor("d1", Role.COURIER, null);
    private final Actor d2 = new Actor("d2", Role.COURIER, null);
    private final List<OrderItemRequest> items = List.of(new OrderItemRequest("m1", 1));

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        Bakery b = new Bakery("b1", "Open", LAT, LON, BakeryStatus.OPEN);
        b.addMenuItem(new MenuItem("m1", "Croissant", 3.5, ItemStatus.AVAILABLE));
        bakeries.save(b);

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryOrderEventRepository eventRepo = new InMemoryOrderEventRepository();
        couriers = new InMemoryCourierRepository();
        CourierManager manager = new CourierManager();
        queue = new DispatchQueue();
        DispatchService dispatch = new DispatchService(couriers, bakeries, orderRepo, queue, manager);
        orders = new OrderService(orderRepo, eventRepo, bakeries, new RegularPricing(), dispatch);
<<<<<<< Updated upstream
        delivery = new DeliveryService(couriers, orderRepo, eventRepo, dispatch, manager,
                order -> fareCalls.incrementAndGet());
=======
        customers = new CustomerLocationRepository();
        delivery = new DeliveryService(couriers, orderRepo, bakeries, customers, eventRepo,
        dispatch, manager, (order, courierId) -> fareCalls.incrementAndGet());

>>>>>>> Stashed changes
    }

    private CourierProfile courier(String id, double lat, CourierStatus status) {
        CourierProfile c = new CourierProfile(id, lat, LON, status);
        couriers.save(c);
        return c;
    }

    private String readyOrder() {
        String id = orders.placeOrder(customer, "b1", items).order().getId();
        orders.acceptOrder(staff, id);
        orders.startPreparing(staff, id);
        orders.markReady(staff, id);
        return id;
    }

    @Test
    void fullDeliveryCycle() {
        CourierProfile c = courier("d1", LAT, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        assertEquals(OrderStatus.PICKED_UP, delivery.confirmPickup(d1).order().getStatus());
        assertEquals(CourierStatus.CARRYING, c.getStatus());
        OrderResult done = delivery.confirmDelivery(d1);
        assertEquals(OrderStatus.DELIVERED, done.order().getStatus());
        assertNotNull(done.event().eventId());
        assertEquals(CourierStatus.IDLE, c.getStatus());
        assertEquals(1, fareCalls.get());
        assertEquals(id, done.order().getId());
    }

    @Test
    void cannotAcceptWithoutAnOffer() {
        courier("d1", LAT, CourierStatus.IDLE);
        assertThrows(BusinessRuleException.class, () -> delivery.accept(d1));
    }

    @Test
    void cannotPickUpBeforeAccepting() {
        courier("d1", LAT, CourierStatus.IDLE);
        readyOrder();
        assertThrows(BusinessRuleException.class, () -> delivery.confirmPickup(d1));
    }

    @Test
    void cannotGoOfflineWhileCarrying() {
        courier("d1", LAT, CourierStatus.IDLE);
        readyOrder();
        delivery.accept(d1);
        delivery.confirmPickup(d1);
        assertThrows(BusinessRuleException.class, () -> delivery.toggleDuty(d1, false));
    }

    @Test
    void goingOnDutyPicksUpAWaitingOrder() {
        CourierProfile c = courier("d1", LAT, CourierStatus.OFFLINE);
        String id = readyOrder();
        delivery.toggleDuty(d1, true);
        assertEquals(id, c.getPendingOfferId());
    }

    @Test
    void declineMovesTheOfferToTheNextCourier() {
        CourierProfile near = courier("d1", LAT + 0.001, CourierStatus.IDLE);
        CourierProfile far = courier("d2", LAT + 0.05, CourierStatus.IDLE);
        String id = readyOrder();
        assertEquals(id, near.getPendingOfferId());
        delivery.decline(d1);
        assertNull(near.getPendingOfferId());
        assertEquals(id, far.getPendingOfferId());
    }

    @Test
    void goingOfflineWithAnOfferReturnsTheOrderToTheQueue() {
        CourierProfile c = courier("d1", LAT, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.toggleDuty(d1, false);
        assertEquals(CourierStatus.OFFLINE, c.getStatus());
        assertNull(c.getPendingOfferId());
        assertEquals(List.of(id), queue.getPending());
    }

    @Test
    void finishingADeliveryOffersTheNextWaitingOrder() {
        CourierProfile c = courier("d1", LAT, CourierStatus.IDLE);
        readyOrder();
        String second = readyOrder();            // d1 holds an offer, so this one waits
        delivery.accept(d1);
        delivery.confirmPickup(d1);
        delivery.confirmDelivery(d1);
        assertEquals(second, c.getPendingOfferId());
    }

    @Test
    void operatorCanReassignAStalledOrder() {
        CourierProfile near = courier("d1", LAT + 0.001, CourierStatus.IDLE);
        CourierProfile far = courier("d2", LAT + 0.05, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        delivery.toggleDuty(d1, false);          // d1 goes offline holding the order
        assertEquals(1, delivery.getStalled(operator).size());

        delivery.reassign(operator, id);
        assertNull(near.getCurrentOrderId());
        assertEquals(id, far.getPendingOfferId());
    }

    @Test
    void cannotReassignFromACourierWhoIsStillOnDuty() {
        courier("d1", LAT, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        assertThrows(BusinessRuleException.class, () -> delivery.reassign(operator, id));
    }

    @Test
    void onlyOperatorsCanReassign() {
        assertThrows(ForbiddenException.class, () -> delivery.reassign(staff, "ORD-0001"));
    }

    @Test
    void onlyCouriersCanToggleDuty() {
        assertThrows(ForbiddenException.class, () -> delivery.toggleDuty(customer, true));
    }

    @Test
    void courierMovesToTheBakeryOnPickupAndToTheCustomerOnDelivery() {
        CourierProfile c = courier("d1", LAT + 0.05, CourierStatus.IDLE);
        customers.save(new CustomerLocation("c1", "Alice", 45.60, -73.50));
        readyOrder();
        delivery.accept(d1);
        delivery.confirmPickup(d1);
        assertEquals(LAT, c.getLatitude(), 1e-9);
        assertEquals(LON, c.getLongitude(), 1e-9);
        delivery.confirmDelivery(d1);
        assertEquals(45.60, c.getLatitude(), 1e-9);
        assertEquals(-73.50, c.getLongitude(), 1e-9);
    }
}