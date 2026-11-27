package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.enums.ItemStatus;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.business.service.Actor;
import com.bakedtogo.business.service.OrderItemRequest;
import com.bakedtogo.business.service.OrderResult;
import com.bakedtogo.business.service.OrderService;
import com.bakedtogo.persistence.memory.InMemoryBakeryRepository;
import com.bakedtogo.persistence.memory.InMemoryOrderEventRepository;
import com.bakedtogo.persistence.memory.InMemoryOrderRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** R1: pins the observable behaviour of OrderService.placeOrder. Must pass before AND after the refactoring. */
class PlaceOrderCharacterisationTest {

    private InMemoryOrderRepository orders;
    private InMemoryOrderEventRepository events;
    private OrderService service;

    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);
    private final Actor staff = new Actor("s1", Role.BAKERY_STAFF, "b1");

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        Bakery open = new Bakery("b1", "Open", 45.52, -73.58, BakeryStatus.OPEN);
        open.addMenuItem(new MenuItem("m1", "Croissant", 3.50, ItemStatus.AVAILABLE));
        open.addMenuItem(new MenuItem("m2", "Pain au chocolat", 3.95, ItemStatus.AVAILABLE));
        open.addMenuItem(new MenuItem("m3", "Danish", 4.25, ItemStatus.SOLD_OUT));
        Bakery paused = new Bakery("b2", "Paused", 45.53, -73.60, BakeryStatus.PAUSED);
        paused.addMenuItem(new MenuItem("m4", "Eclair", 4.50, ItemStatus.AVAILABLE));
        Bakery closed = new Bakery("b3", "Closed", 45.54, -73.61, BakeryStatus.CLOSED);
        closed.addMenuItem(new MenuItem("m5", "Baguette", 2.75, ItemStatus.AVAILABLE));
        bakeries.save(open);
        bakeries.save(paused);
        bakeries.save(closed);
        orders = new InMemoryOrderRepository();
        events = new InMemoryOrderEventRepository();
        service = new OrderService(orders, events, bakeries);
    }

    private OrderResult place(String bakeryId, OrderItemRequest... items) {
        return service.placeOrder(customer, bakeryId, List.of(items));
    }

    private OrderItemRequest line(String id, int quantity) {
        return new OrderItemRequest(id, quantity);
    }

    private void assertRefused(Class<? extends RuntimeException> type, String message,
                               String bakeryId, OrderItemRequest... items) {
        RuntimeException ex = assertThrows(type, () -> place(bakeryId, items));
        assertEquals(message, ex.getMessage());
        assertTrue(orders.findAll().isEmpty(), "a refused placement must not store an order");
        assertTrue(events.findAll().isEmpty(), "a refused placement must not record an event");
    }

    // ---------- success ----------

    @Test
    void validOrderIsPlacedWithFirstIdLinesAndSummedTotal() {
        OrderResult r = place("b1", line("m1", 2), line("m2", 1));
        assertEquals("ORD-0001", r.order().getId());
        assertEquals(OrderStatus.PLACED, r.order().getStatus());
        assertEquals("c1", r.order().getCustomerId());
        assertEquals("b1", r.order().getBakeryId());
        assertEquals(2, r.order().getLines().size());
        assertEquals("m1", r.order().getLines().get(0).menuItemId());
        assertEquals("Croissant", r.order().getLines().get(0).name());
        assertEquals(3.50, r.order().getLines().get(0).unitPrice(), 1e-9);
        assertEquals(2, r.order().getLines().get(0).quantity());
        assertEquals("m2", r.order().getLines().get(1).menuItemId());
        assertEquals(10.95, r.order().getTotal(), 1e-9);
        assertTrue(orders.findById("ORD-0001").isPresent());
    }

    @Test
    void idsAreSequentialPerService() {
        assertEquals("ORD-0001", place("b1", line("m1", 1)).order().getId());
        assertEquals("ORD-0002", place("b1", line("m1", 1)).order().getId());
    }

    @Test
    void placementRecordsExactlyOneEventFromNothingToPlaced() {
        OrderResult r = place("b1", line("m1", 1));
        List<OrderEvent> recorded = events.findByOrderId("ORD-0001");
        assertEquals(1, recorded.size());
        OrderEvent e = recorded.get(0);
        assertNull(e.fromStatus());
        assertEquals(OrderStatus.PLACED, e.toStatus());
        assertEquals("c1", e.actorId());
        assertEquals(r.event().eventId(), e.eventId());
    }

    // ---------- refusals, with their exact messages ----------

    @Test
    void pausedAndClosedBakeriesAreRefused() {
        assertRefused(BusinessRuleException.class, "Bakery Paused is paused and not accepting orders", "b2", line("m4", 1));
        assertRefused(BusinessRuleException.class, "Bakery Closed is closed and not accepting orders", "b3", line("m5", 1));
    }

    @Test
    void emptyOrMissingItemListIsRefused() {
        BusinessRuleException a = assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b1", List.of()));
        BusinessRuleException b = assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b1", null));
        assertEquals("An order needs at least one item", a.getMessage());
        assertEquals("An order needs at least one item", b.getMessage());
    }

    @Test
    void unknownItemAndSoldOutItemAreRefused() {
        assertRefused(NotFoundException.class, "Menu item m9 not found at Open", "b1", line("m9", 1));
        assertRefused(BusinessRuleException.class, "Item Danish is sold out", "b1", line("m3", 1));
    }

    @Test
    void nonPositiveQuantityIsRefused() {
        assertRefused(BusinessRuleException.class, "Quantity must be positive for item m1", "b1", line("m1", 0));
        assertRefused(BusinessRuleException.class, "Quantity must be positive for item m1", "b1", line("m1", -2));
    }

    // ---------- the ORDER of the checks is part of the behaviour ----------

    @Test
    void onlyCustomersMayPlaceAndTheRoleIsCheckedFirst() {
        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> service.placeOrder(staff, "zzz", List.of()));
        assertEquals("Role BAKERY_STAFF may not perform this action", ex.getMessage());
    }

    @Test
    void unknownBakeryIsCheckedBeforeStatusAndItems() {
        assertRefused(NotFoundException.class, "Bakery zzz not found", "zzz");
    }

    @Test
    void bakeryStatusIsCheckedBeforeTheItemList() {
        assertRefused(BusinessRuleException.class, "Bakery Paused is paused and not accepting orders", "b2");
    }

    @Test
    void theFirstProblemItemWins() {
        assertRefused(BusinessRuleException.class, "Item Danish is sold out", "b1", line("m3", 1), line("m9", 1));
        assertRefused(NotFoundException.class, "Menu item m9 not found at Open", "b1", line("m9", 1), line("m3", 1));
    }

    @Test
    void availabilityIsCheckedBeforeQuantity() {
        assertRefused(BusinessRuleException.class, "Item Danish is sold out", "b1", line("m3", 0));
    }

    @Test
    void aRefusedAttemptDoesNotConsumeAnOrderId() {
        assertThrows(BusinessRuleException.class, () -> place("b2", line("m4", 1)));
        assertEquals("ORD-0001", place("b1", line("m1", 1)).order().getId());
    }
}