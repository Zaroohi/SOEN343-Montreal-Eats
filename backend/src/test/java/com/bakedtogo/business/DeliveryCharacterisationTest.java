package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.dispatch.CourierManager;
import com.bakedtogo.business.dispatch.DispatchQueue;
import com.bakedtogo.business.dispatch.DispatchService;
import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.CustomerLocation;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.enums.ItemStatus;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.business.service.Actor;
import com.bakedtogo.business.service.DeliveryService;
import com.bakedtogo.business.service.OrderItemRequest;
import com.bakedtogo.business.service.OrderResult;
import com.bakedtogo.business.service.OrderService;
import com.bakedtogo.persistence.CustomerLocationRepository;
import com.bakedtogo.persistence.memory.InMemoryBakeryRepository;
import com.bakedtogo.persistence.memory.InMemoryCourierRepository;
import com.bakedtogo.persistence.memory.InMemoryOrderEventRepository;
import com.bakedtogo.persistence.memory.InMemoryOrderRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** R2: pins confirmPickup, confirmDelivery and reassign. Must pass before AND after the refactoring. */
class DeliveryCharacterisationTest {

    private static final double B_LAT = 45.5236, B_LON = -73.5817;   // bakery b1
    private static final double C_LAT = 45.5100, C_LON = -73.5800;   // customer c1

    private InMemoryCourierRepository couriers;
    private InMemoryOrderEventRepository events;
    private DispatchQueue queue;
    private OrderService orders;
    private DeliveryService delivery;
    private final List<String> billed = new ArrayList<>();

    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);
    private final Actor staff = new Actor("s1", Role.BAKERY_STAFF, "b1");
    private final Actor operator = new Actor("o1", Role.OPERATOR, null);
    private final Actor d1 = new Actor("d1", Role.COURIER, null);
    private final List<OrderItemRequest> items = List.of(new OrderItemRequest("m1", 1));

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        Bakery b = new Bakery("b1", "Open", B_LAT, B_LON, BakeryStatus.OPEN);
        b.addMenuItem(new MenuItem("m1", "Croissant", 3.5, ItemStatus.AVAILABLE));
        bakeries.save(b);
        CustomerLocationRepository customers = new CustomerLocationRepository();
        customers.save(new CustomerLocation("c1", "Alice", C_LAT, C_LON));
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        events = new InMemoryOrderEventRepository();
        couriers = new InMemoryCourierRepository();
        CourierManager manager = new CourierManager();
        queue = new DispatchQueue();
        DispatchService dispatch = new DispatchService(couriers, bakeries, orderRepo, queue, manager);
        orders = new OrderService(orderRepo, events, bakeries, new RegularPricing(), dispatch);
        delivery = new DeliveryService(couriers, orderRepo, bakeries, customers, events, dispatch, manager,
                (order, courierId) -> billed.add(order.getId() + ":" + courierId));
    }

    private CourierProfile courier(String id, double lat, CourierStatus status) {
        CourierProfile c = new CourierProfile(id, lat, B_LON, status);
        couriers.save(c);
        return c;
    }

    private String readyOrder() {
        String id = orders.placeOrder(customer, "b1", items, null).order().getId();
        orders.acceptOrder(staff, id);
        orders.startPreparing(staff, id);
        orders.markReady(staff, id);
        return id;
    }

    private String refusal(Executable call) {
        return assertThrows(BusinessRuleException.class, call).getMessage();
    }

    // ---------- confirmPickup ----------

    @Test
    void pickupIsRefusedUnlessTheCourierIsAssigned() {
        courier("d1", B_LAT, CourierStatus.IDLE);
        assertEquals("No assigned order to pick up (IDLE)", refusal(() -> delivery.confirmPickup(d1)));
        readyOrder();   // an offer is pending but not accepted yet
        assertEquals("No assigned order to pick up (IDLE)", refusal(() -> delivery.confirmPickup(d1)));
    }

    @Test
    void pickupIsRefusedForAnOfflineCourier() {
        courier("d1", B_LAT, CourierStatus.OFFLINE);
        assertEquals("No assigned order to pick up (OFFLINE)", refusal(() -> delivery.confirmPickup(d1)));
    }

    @Test
    void pickupMovesOrderAndCourierForwardAndPlacesTheCourierAtTheBakery() {
        CourierProfile c = courier("d1", B_LAT + 0.05, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        OrderResult r = delivery.confirmPickup(d1);
        assertEquals(OrderStatus.PICKED_UP, r.order().getStatus());
        assertEquals(CourierStatus.CARRYING, c.getStatus());
        assertEquals(B_LAT, c.getLatitude(), 1e-9);
        assertEquals(B_LON, c.getLongitude(), 1e-9);
        OrderEvent e = r.event();
        assertEquals(id, e.orderId());
        assertEquals(OrderStatus.READY, e.fromStatus());
        assertEquals(OrderStatus.PICKED_UP, e.toStatus());
        assertEquals("d1", e.actorId());
        assertTrue(events.findByOrderId(id).contains(e));
    }

    @Test
    void pickupWhileCarryingIsRefused() {
        courier("d1", B_LAT, CourierStatus.IDLE);
        readyOrder();
        delivery.accept(d1);
        delivery.confirmPickup(d1);
        assertEquals("No assigned order to pick up (CARRYING)", refusal(() -> delivery.confirmPickup(d1)));
    }

    // ---------- confirmDelivery ----------

    @Test
    void deliveryIsRefusedUnlessTheCourierIsCarrying() {
        courier("d1", B_LAT, CourierStatus.IDLE);
        assertEquals("Not carrying an order (IDLE)", refusal(() -> delivery.confirmDelivery(d1)));
        readyOrder();
        delivery.accept(d1);
        assertEquals("Not carrying an order (ASSIGNED)", refusal(() -> delivery.confirmDelivery(d1)));
    }

    @Test
    void deliveryClosesTheOrderBillsOnceFreesTheCourierAndPlacesThemAtTheCustomer() {
        CourierProfile c = courier("d1", B_LAT, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        delivery.confirmPickup(d1);
        OrderResult r = delivery.confirmDelivery(d1);
        assertEquals(OrderStatus.DELIVERED, r.order().getStatus());
        assertEquals(CourierStatus.IDLE, c.getStatus());
        assertNull(c.getCurrentOrderId());
        assertTrue(c.isAvailable());
        assertEquals(C_LAT, c.getLatitude(), 1e-9);
        assertEquals(C_LON, c.getLongitude(), 1e-9);
        assertEquals(List.of(id + ":d1"), billed);
        assertEquals(OrderStatus.PICKED_UP, r.event().fromStatus());
        assertEquals(OrderStatus.DELIVERED, r.event().toStatus());
        assertEquals("d1", r.event().actorId());
    }

    @Test
    void deliveryOffersTheNextWaitingOrderToTheFreedCourier() {
        CourierProfile c = courier("d1", B_LAT, CourierStatus.IDLE);
        readyOrder();
        String second = readyOrder();   // d1 already holds an offer, so this one waits
        delivery.accept(d1);
        delivery.confirmPickup(d1);
        delivery.confirmDelivery(d1);
        assertEquals(second, c.getPendingOfferId());
        assertTrue(queue.getPending().isEmpty());
    }

    // ---------- reassign ----------

    @Test
    void reassignNeedsAnOperator() {
        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> delivery.reassign(staff, "ORD-0001"));
        assertEquals("Role BAKERY_STAFF may not perform this action", ex.getMessage());
    }

    @Test
    void reassignOfAnUnknownOrderIsNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> delivery.reassign(operator, "ORD-9999"));
        assertEquals("Order ORD-9999 not found", ex.getMessage());
    }

    @Test
    void reassignOfAnOrderNobodyHoldsIsRefused() {
        String id = readyOrder();   // no courier exists, so it waits in the queue
        assertEquals("Order " + id + " is not assigned to any courier",
                refusal(() -> delivery.reassign(operator, id)));
    }

    @Test
    void reassignIsRefusedWhileTheHolderIsStillOnDuty() {
        courier("d1", B_LAT, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        assertEquals("Courier d1 is ASSIGNED, not offline", refusal(() -> delivery.reassign(operator, id)));
    }

    @Test
    void reassignReleasesTheOrderFromTheOfflineCourierAndRedispatchesIt() {
        CourierProfile near = courier("d1", B_LAT + 0.001, CourierStatus.IDLE);
        CourierProfile far = courier("d2", B_LAT + 0.05, CourierStatus.IDLE);
        String id = readyOrder();
        delivery.accept(d1);
        delivery.toggleDuty(d1, false);
        assertEquals(id, near.getCurrentOrderId());

        Order returned = delivery.reassign(operator, id);

        assertEquals(id, returned.getId());
        assertNull(near.getCurrentOrderId());
        assertEquals(CourierStatus.OFFLINE, near.getStatus());
        assertEquals(id, far.getPendingOfferId());
    }
}