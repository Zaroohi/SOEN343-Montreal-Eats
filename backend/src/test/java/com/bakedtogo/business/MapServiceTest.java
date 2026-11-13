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
import com.bakedtogo.persistence.memory.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapServiceTest {

    private MapService map;

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        bakeries.save(new Bakery("b1", "Open", 45.52, -73.58, BakeryStatus.OPEN));
        bakeries.save(new Bakery("b2", "Paused", 45.53, -73.60, BakeryStatus.PAUSED));

        InMemoryCourierRepository couriers = new InMemoryCourierRepository();
        CourierProfile d1 = new CourierProfile("d1", 45.50, -73.57, CourierStatus.IDLE);
        couriers.save(d1);
        couriers.save(new CourierProfile("d2", 45.53, -73.60, CourierStatus.IDLE));

        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        orders.save(new Order("ORD-1", "c1", "b1",
                List.of(new OrderLine("m1", "Croissant", 3.5, 1))));
        d1.offer("ORD-1");

        CustomerLocationRepository customers = new CustomerLocationRepository();
        customers.save(new CustomerLocation("c1", "Alice", 45.51, -73.58));
        customers.save(new CustomerLocation("c2", "Bob", 45.54, -73.61));

        map = new MapService(bakeries, couriers, orders, customers, new DispatchQueue());
    }

    @Test
    void operatorSeesEverything() {
        MapView v = map.view(new Actor("o1", Role.OPERATOR, null));
        assertEquals(2, v.bakeries().size());
        assertEquals(2, v.couriers().size());
        assertEquals(2, v.customers().size());
    }

    @Test
    void customerSeesOnlyTheCourierOnTheirOrder() {
        MapView v = map.view(new Actor("c1", Role.CUSTOMER, null));
        assertEquals(1, v.couriers().size());
        assertEquals("d1", v.couriers().get(0).id());
        assertEquals(1, v.customers().size());
    }

    @Test
    void otherCustomerSeesNoCouriers() {
        MapView v = map.view(new Actor("c2", Role.CUSTOMER, null));
        assertTrue(v.couriers().isEmpty());
    }

    @Test
    void courierSeesOnlyThemselvesAndTheOfferedBakery() {
        MapView v = map.view(new Actor("d1", Role.COURIER, null));
        assertEquals(1, v.couriers().size());
        assertEquals("offer", v.couriers().get(0).targetType());
        assertEquals(1, v.bakeries().size());
        assertEquals("b1", v.bakeries().get(0).id());
    }

    @Test
    void courierWithoutAnOrderSeesNoBakery() {
        MapView v = map.view(new Actor("d2", Role.COURIER, null));
        assertTrue(v.bakeries().isEmpty());
    }

    @Test
    void staffSeesOnlyTheirOwnBakery() {
        MapView v = map.view(new Actor("s1", Role.BAKERY_STAFF, "b1"));
        assertEquals(1, v.bakeries().size());
        assertEquals("d1", v.couriers().get(0).id());
    }

    @Test
    void emptyCityGivesAnEmptyMapInsteadOfAnError() {
        MapService empty = new MapService(new InMemoryBakeryRepository(), new InMemoryCourierRepository(),
                new InMemoryOrderRepository(), new CustomerLocationRepository(), new DispatchQueue());
        MapView v = empty.view(new Actor("o1", Role.OPERATOR, null));
        assertTrue(v.bakeries().isEmpty());
        assertTrue(v.couriers().isEmpty());
        assertTrue(v.customers().isEmpty());
        assertTrue(v.dispatchQueue().isEmpty());
    }

    @Test
    void courierWithoutAProfileGetsAnEmptyMap() {
        MapView v = map.view(new Actor("d9", Role.COURIER, null));
        assertTrue(v.couriers().isEmpty());
        assertTrue(v.bakeries().isEmpty());
    }
}