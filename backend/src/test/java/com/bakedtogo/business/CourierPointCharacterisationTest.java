package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.dispatch.DispatchQueue;
import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.CustomerLocation;
import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.service.Actor;
import com.bakedtogo.business.service.MapService;
import com.bakedtogo.business.service.MapView;
import com.bakedtogo.persistence.CustomerLocationRepository;
import com.bakedtogo.persistence.memory.InMemoryBakeryRepository;
import com.bakedtogo.persistence.memory.InMemoryCourierRepository;
import com.bakedtogo.persistence.memory.InMemoryOrderRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** R3: pins how MapService describes a courier's target. Must pass before AND after the refactoring. */
class CourierPointCharacterisationTest {

    private static final double B_LAT = 45.52, B_LON = -73.58;     // bakery b1
    private static final double C_LAT = 45.51, C_LON = -73.585;    // customer c1

    private InMemoryBakeryRepository bakeries;
    private InMemoryCourierRepository couriers;
    private CustomerLocationRepository customers;
    private MapService map;
    private final Actor operator = new Actor("o1", Role.OPERATOR, null);

    @BeforeEach
    void setUp() {
        bakeries = new InMemoryBakeryRepository();
        bakeries.save(new Bakery("b1", "Open", B_LAT, B_LON, BakeryStatus.OPEN));
        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        orders.save(new Order("ORD-1", "c1", "b1", List.of(new OrderLine("m1", "Croissant", 3.5, 1))));
        customers = new CustomerLocationRepository();
        customers.save(new CustomerLocation("c1", "Alice", C_LAT, C_LON));
        couriers = new InMemoryCourierRepository();
        map = new MapService(bakeries, couriers, orders, customers, new DispatchQueue());
    }

    private CourierProfile courier(String id, CourierStatus status) {
        CourierProfile c = new CourierProfile(id, 45.50, -73.57, status);
        couriers.save(c);
        return c;
    }

    private MapView.CourierPoint point(String id) {
        return map.view(operator).couriers().stream()
                .filter(c -> c.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void idleCourierWithoutAnOrderHasNoTarget() {
        courier("d1", CourierStatus.IDLE);
        MapView.CourierPoint p = point("d1");
        assertEquals("IDLE", p.status());
        assertEquals(45.50, p.lat(), 1e-9);
        assertEquals(-73.57, p.lon(), 1e-9);
        assertNull(p.orderId());
        assertNull(p.targetType());
        assertNull(p.targetLat());
        assertNull(p.targetLon());
        assertEquals(0, p.legElapsedMillis());
    }

    @Test
    void offeredOrderPointsAtTheBakeryAsAnOffer() {
        courier("d1", CourierStatus.IDLE).offer("ORD-1");
        MapView.CourierPoint p = point("d1");
        assertEquals("ORD-1", p.orderId());
        assertEquals("offer", p.targetType());
        assertEquals(B_LAT, p.targetLat(), 1e-9);
        assertEquals(B_LON, p.targetLon(), 1e-9);
        assertEquals(0, p.legElapsedMillis());
    }

    @Test
    void acceptedOrderPointsAtTheBakeryAsPickup() {
        CourierProfile c = courier("d1", CourierStatus.IDLE);
        c.offer("ORD-1");
        c.accept();
        MapView.CourierPoint p = point("d1");
        assertEquals("ASSIGNED", p.status());
        assertEquals("pickup", p.targetType());
        assertEquals(B_LAT, p.targetLat(), 1e-9);
        assertEquals(B_LON, p.targetLon(), 1e-9);
        assertTrue(p.legElapsedMillis() >= 0);
    }

    @Test
    void carriedOrderPointsAtTheCustomerAsDelivery() {
        CourierProfile c = courier("d1", CourierStatus.IDLE);
        c.offer("ORD-1");
        c.accept();
        c.pickUp();
        MapView.CourierPoint p = point("d1");
        assertEquals("CARRYING", p.status());
        assertEquals("delivery", p.targetType());
        assertEquals(C_LAT, p.targetLat(), 1e-9);
        assertEquals(C_LON, p.targetLon(), 1e-9);
    }

    @Test
    void offlineCourierHoldingAnOrderHasNoTargetButKeepsTheOrderId() {
        CourierProfile c = courier("d1", CourierStatus.IDLE);
        c.offer("ORD-1");
        c.accept();
        c.goOffDuty();
        MapView.CourierPoint p = point("d1");
        assertEquals("OFFLINE", p.status());
        assertEquals("ORD-1", p.orderId());
        assertNull(p.targetType());
        assertNull(p.targetLat());
        assertNull(p.targetLon());
    }

    @Test
    void missingCustomerLocationGivesNoTargetWhileCarrying() {
        CourierProfile c = courier("d1", CourierStatus.IDLE);
        c.offer("ORD-1");
        c.accept();
        c.pickUp();
        customers.reset();
        MapView.CourierPoint p = point("d1");
        assertEquals("ORD-1", p.orderId());
        assertNull(p.targetType());
        assertNull(p.targetLat());
    }

    @Test
    void missingBakeryGivesNoTargetForAnOffer() {
        courier("d1", CourierStatus.IDLE).offer("ORD-1");
        bakeries.reset();
        MapView.CourierPoint p = point("d1");
        assertEquals("ORD-1", p.orderId());
        assertNull(p.targetType());
        assertNull(p.targetLon());
    }
}