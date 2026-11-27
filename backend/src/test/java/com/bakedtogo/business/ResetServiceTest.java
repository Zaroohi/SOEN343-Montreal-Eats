package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.dispatch.*;
import com.bakedtogo.business.domain.enums.*;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.business.service.*;
import com.bakedtogo.infrastructure.CityLoader;
import com.bakedtogo.persistence.CustomerLocationRepository;
import com.bakedtogo.persistence.memory.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResetServiceTest {

    private InMemoryBakeryRepository bakeries;
    private InMemoryCourierRepository couriers;
    private InMemoryOrderRepository orderRepo;
    private InMemoryOrderEventRepository eventRepo;
    private DispatchQueue queue;
    private OrderService orders;
    private ResetService reset;

    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);
    private final Actor staff = new Actor("s1", Role.BAKERY_STAFF, "b1");
    private final Actor operator = new Actor("o1", Role.OPERATOR, null);
    private final List<OrderItemRequest> items = List.of(new OrderItemRequest("m1", 1));

    @BeforeEach
    void setUp() {
        bakeries = new InMemoryBakeryRepository();
        couriers = new InMemoryCourierRepository();
        CustomerLocationRepository customers = new CustomerLocationRepository();
        orderRepo = new InMemoryOrderRepository();
        eventRepo = new InMemoryOrderEventRepository();
        CourierManager manager = new CourierManager();
        queue = new DispatchQueue();
        DispatchService dispatch = new DispatchService(couriers, bakeries, orderRepo, queue, manager);
        orders = new OrderService(orderRepo, eventRepo, bakeries, new RegularPricing(), dispatch);
        CityLoader city = new CityLoader(bakeries, couriers, customers);
        city.load();
        reset = new ResetService(
                List.of(bakeries, couriers, customers, orderRepo, eventRepo, queue, dispatch, orders), city);
    }

    @Test
    void resetErasesOrdersEventsAndTheDispatchQueue() {
        couriers.findAll().forEach(c -> { if (c.getStatus() != CourierStatus.OFFLINE) c.goOffDuty(); });
        String id = orders.placeOrder(customer, "b1", items,null).order().getId();
        orders.acceptOrder(staff, id);
        orders.startPreparing(staff, id);
        orders.markReady(staff, id);          // nobody is on duty, so the order waits in the queue
        assertFalse(queue.getPending().isEmpty());

        reset.reset(operator);

        assertTrue(orderRepo.findAll().isEmpty());
        assertTrue(eventRepo.findAll().isEmpty());
        assertTrue(queue.getPending().isEmpty());
    }

    @Test
    void resetRestoresBakeriesAndCouriersFromCityJson() {
        bakeries.findById("b1").orElseThrow().setStatus(BakeryStatus.PAUSED);
        bakeries.findById("b1").orElseThrow().getMenu().get(0).setStatus(ItemStatus.SOLD_OUT);
        couriers.findById("d1").orElseThrow().goOnDuty();

        reset.reset(operator);

        assertEquals(BakeryStatus.OPEN, bakeries.findById("b1").orElseThrow().getStatus());
        assertEquals(ItemStatus.AVAILABLE,
                bakeries.findById("b1").orElseThrow().getMenu().get(0).getStatus());
        assertEquals(CourierStatus.OFFLINE, couriers.findById("d1").orElseThrow().getStatus());
    }

    @Test
    void idsStartOverAfterAReset() {
        orders.placeOrder(customer, "b1", items,null);
        reset.reset(operator);
        OrderResult again = orders.placeOrder(customer, "b1", items,null);
        assertEquals("ORD-0001", again.order().getId());
        assertEquals("EVT-0001", again.event().eventId());
    }

    @Test
    void onlyOperatorsCanReset() {
        assertThrows(ForbiddenException.class, () -> reset.reset(customer));
        assertThrows(ForbiddenException.class, () -> reset.reset(staff));
    }
}