package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.business.pricing.PricingStrategy;
import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.infrastructure.EventLogger;
import com.bakedtogo.persistence.BakeryRepository;
import com.bakedtogo.persistence.OrderEventRepository;
import com.bakedtogo.persistence.OrderRepository;
import com.bakedtogo.business.dispatch.DispatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OrderEventRepository events;
    private final BakeryRepository bakeries;
    private final PricingStrategy pricing;
    private final DispatchService dispatch;
    private final AtomicLong orderCounter = new AtomicLong();

    /** Keeps existing tests working: regular pricing, no dispatch. */
    public OrderService(OrderRepository orders, OrderEventRepository events,
                        BakeryRepository bakeries) {
        this(orders, events, bakeries, new RegularPricing(), null);
    }

    @Autowired
    public OrderService(OrderRepository orders, OrderEventRepository events,
                        BakeryRepository bakeries, PricingStrategy pricing,
                        DispatchService dispatch) {
        this.orders = orders;
        this.events = events;
        this.bakeries = bakeries;
        this.pricing = pricing;
        this.dispatch = dispatch;
    }

    // ---------- customer commands ----------

    /** UC-ORD-03. Enforces R-ORD-02. */
    public synchronized OrderResult placeOrder(Actor actor, String bakeryId,
                                               List<OrderItemRequest> items) {
        requireRole(actor, Role.CUSTOMER);
        Bakery bakery = findBakery(bakeryId);
        if (bakery.getStatus() != BakeryStatus.OPEN) {
            throw new BusinessRuleException("Bakery " + bakery.getName() + " is "
                    + bakery.getStatus().name().toLowerCase() + " and not accepting orders");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessRuleException("An order needs at least one item");
        }
        List<OrderLine> lines = new ArrayList<>();
        for (OrderItemRequest item : items) {
            MenuItem menuItem = bakery.getMenu().stream()
                    .filter(m -> m.getId().equals(item.menuItemId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(
                            "Menu item " + item.menuItemId() + " not found at " + bakery.getName()));
            if (!menuItem.isAvailable()) {
                throw new BusinessRuleException("Item " + menuItem.getName() + " is sold out");
            }
            lines.add(new OrderLine(menuItem.getId(), menuItem.getName(),
                    menuItem.getPrice(), item.quantity()));
        }
        Order order = new Order(nextOrderId(), actor.id(), bakeryId, lines);
        order.setPricingBreakdown(pricing.calculateBreakdown(lines));
        orders.save(order);
        return new OrderResult(order, record(order, null, actor));
    }

    /** UC-ORD-04. Customers cancel their own order, only while PLACED (the state enforces it). */
    public synchronized OrderResult cancelOrder(Actor actor, String orderId) {
        requireRole(actor, Role.CUSTOMER);
        Order order = findOrder(orderId);
        if (!order.getCustomerId().equals(actor.id())) {
            throw new ForbiddenException("Customers can only cancel their own orders");
        }
        OrderStatus from = order.getStatus();
        order.cancel();
        return new OrderResult(order, record(order, from, actor));
    }

    // ---------- bakery staff commands ----------

    /** UC-ORD-07. Accepted orders join the tail of the bakery's queue (R-ORD-03). */
    public synchronized OrderResult acceptOrder(Actor actor, String orderId) {
        Order order = staffOrder(actor, orderId);
        OrderStatus from = order.getStatus();
        order.accept();
        findBakery(order.getBakeryId()).enqueue(order);
        return new OrderResult(order, record(order, from, actor));
    }

    public synchronized OrderResult rejectOrder(Actor actor, String orderId, String reason) {
        Order order = staffOrder(actor, orderId);
        OrderStatus from = order.getStatus();
        order.reject(reason);
        return new OrderResult(order, record(order, from, actor));
    }

    /** Only the head of the queue may start (R-ORD-03). */
    public synchronized OrderResult startPreparing(Actor actor, String orderId) {
        Order order = staffOrder(actor, orderId);
        OrderStatus from = order.getStatus();
        if (from == OrderStatus.ACCEPTED) {
            findBakery(order.getBakeryId()).checkIsNext(order);
        }
        order.startPreparing();      // throws IllegalTransitionException if not ACCEPTED
        return new OrderResult(order, record(order, from, actor));
    }

    public synchronized OrderResult markReady(Actor actor, String orderId) {
    Order order = staffOrder(actor, orderId);
    OrderStatus from = order.getStatus();
    order.markReady();           // throws if not PREPARING (R-ORD-04)
    findBakery(order.getBakeryId()).dequeue(order);
    OrderResult result = new OrderResult(order, record(order, from, actor));
    if (dispatch != null) dispatch.dispatch(order.getId());   // UC-DLV-02
    return result;
}

    // ---------- queries ----------

    /** UC-ORD-05. Customers see only their own orders, staff only their bakery's. */
    public Order getOrder(Actor actor, String orderId) {
        Order order = findOrder(orderId);
        boolean allowed = switch (actor.role()) {
            case CUSTOMER -> order.getCustomerId().equals(actor.id());
            case BAKERY_STAFF -> order.getBakeryId().equals(actor.bakeryId());
            case OPERATOR -> true;
            default -> false;
        };
        if (!allowed) throw new ForbiddenException("You may not view this order");
        return order;
    }

    public List<Order> getOrders(Actor actor) {
        return switch (actor.role()) {
            case CUSTOMER -> orders.findByCustomerId(actor.id());
            case BAKERY_STAFF -> orders.findByBakeryId(actor.bakeryId());
            case OPERATOR -> orders.findAll();
            default -> throw new ForbiddenException("This role has no order list");
        };
    }

    public List<OrderEvent> getEvents(String orderId) {
        return events.findByOrderId(orderId);
    }

    // ---------- helpers ----------

    private OrderEvent record(Order order, OrderStatus from, Actor actor) {
        OrderEvent event = EventLogger.getInstance()
                .record(order.getId(), from, order.getStatus(), actor.id());
        events.save(event);
        return event;
    }

    private Order staffOrder(Actor actor, String orderId) {
        requireRole(actor, Role.BAKERY_STAFF);
        Order order = findOrder(orderId);
        if (!order.getBakeryId().equals(actor.bakeryId())) {
            throw new ForbiddenException("Staff can only act on orders of their own bakery");
        }
        return order;
    }

    private void requireRole(Actor actor, Role required) {
        if (actor == null || actor.role() != required) {
            throw new ForbiddenException("Role " + (actor == null ? "none" : actor.role())
                    + " may not perform this action");
        }
    }

    private Bakery findBakery(String id) {
        return bakeries.findById(id)
                .orElseThrow(() -> new NotFoundException("Bakery " + id + " not found"));
    }

    private Order findOrder(String id) {
        return orders.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    private String nextOrderId() {
        return "ORD-" + String.format("%04d", orderCounter.incrementAndGet());
    }
}