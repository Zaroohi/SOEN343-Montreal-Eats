package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.enums.*;
import com.bakedtogo.business.domain.exception.*;
import com.bakedtogo.business.service.*;
import com.bakedtogo.persistence.memory.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private OrderService service;
    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);
    private final Actor staff = new Actor("s1", Role.BAKERY_STAFF, "b1");
    private final Actor otherStaff = new Actor("s2", Role.BAKERY_STAFF, "b2");
    private final List<OrderItemRequest> croissants = List.of(new OrderItemRequest("m1", 2));

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        Bakery open = new Bakery("b1", "Open", 0, 0, BakeryStatus.OPEN);
        open.addMenuItem(new MenuItem("m1", "Croissant", 3.5, ItemStatus.AVAILABLE));
        open.addMenuItem(new MenuItem("m3", "Danish", 4.25, ItemStatus.SOLD_OUT));
        Bakery paused = new Bakery("b2", "Paused", 0, 0, BakeryStatus.PAUSED);
        paused.addMenuItem(new MenuItem("m4", "Eclair", 4.5, ItemStatus.AVAILABLE));
        bakeries.save(open);
        bakeries.save(paused);
        service = new OrderService(new InMemoryOrderRepository(),
                new InMemoryOrderEventRepository(), bakeries);
    }

    @Test
    void placingAnOrderRecordsAnEvent() {
        OrderResult r = service.placeOrder(customer, "b1", croissants,null);
        assertEquals(OrderStatus.PLACED, r.order().getStatus());
        assertNotNull(r.event().eventId());
    }

    @Test
    void soldOutItemIsRefused() {
        assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b1", List.of(new OrderItemRequest("m3", 1)),null));
    }

    @Test
    void pausedBakeryIsRefused() {
        assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b2", List.of(new OrderItemRequest("m4", 1)),null));
    }

    @Test
    void customerCannotAccept() {
        String id = service.placeOrder(customer, "b1", croissants,null).order().getId();
        assertThrows(ForbiddenException.class, () -> service.acceptOrder(customer, id));
    }

    @Test
    void otherBakeryStaffCannotAccept() {
        String id = service.placeOrder(customer, "b1", croissants,null).order().getId();
        assertThrows(ForbiddenException.class, () -> service.acceptOrder(otherStaff, id));
    }

    @Test
    void cannotMarkReadyStraightFromAccepted() {
        String id = service.placeOrder(customer, "b1", croissants,null).order().getId();
        service.acceptOrder(staff, id);
        assertThrows(IllegalTransitionException.class, () -> service.markReady(staff, id));
    }

    @Test
    void queueIsFirstInFirstOut() {
        String first = service.placeOrder(customer, "b1", croissants,null).order().getId();
        String second = service.placeOrder(customer, "b1", croissants,null).order().getId();
        service.acceptOrder(staff, first);
        service.acceptOrder(staff, second);

        assertThrows(BusinessRuleException.class, () -> service.startPreparing(staff, second));

        service.startPreparing(staff, first);
        service.markReady(staff, first);
        assertEquals(OrderStatus.PREPARING, service.startPreparing(staff, second).order().getStatus());
    }

    @Test
    void everyTransitionHasItsOwnEventId() {
        String id = service.placeOrder(customer, "b1", croissants,null).order().getId();
        service.acceptOrder(staff, id);
        service.startPreparing(staff, id);
        service.markReady(staff, id);
        var events = service.getEvents(id);
        assertEquals(4, events.size());
        assertEquals(4, events.stream().map(e -> e.eventId()).distinct().count());
    }

    @Test
    void cannotCancelAfterAcceptance() {
        String id = service.placeOrder(customer, "b1", croissants,null).order().getId();
        service.acceptOrder(staff, id);
        assertThrows(IllegalTransitionException.class, () -> service.cancelOrder(customer, id));
    }
}