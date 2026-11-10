package com.bakedtogo.business.service;

import com.bakedtogo.business.billing.FareNotifier;
import com.bakedtogo.business.dispatch.CourierManager;
import com.bakedtogo.business.dispatch.DispatchService;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.infrastructure.EventLogger;
import com.bakedtogo.persistence.CourierRepository;
import com.bakedtogo.persistence.OrderEventRepository;
import com.bakedtogo.persistence.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** DLV use cases UC-DLV-01, 03, 05, 06, 07. */
@Service
public class DeliveryService {

    private final CourierRepository couriers;
    private final OrderRepository orders;
    private final OrderEventRepository events;
    private final DispatchService dispatch;
    private final CourierManager manager;
    private final FareNotifier fares;

    public DeliveryService(CourierRepository couriers, OrderRepository orders,
                           OrderEventRepository events, DispatchService dispatch,
                           CourierManager manager, FareNotifier fares) {
        this.couriers = couriers;
        this.orders = orders;
        this.events = events;
        this.dispatch = dispatch;
        this.manager = manager;
        this.fares = fares;
    }

    // ---------- courier commands ----------

    /** UC-DLV-01. A courier cannot go offline while carrying (R-DLV-03). */
    public synchronized CourierView toggleDuty(Actor actor, boolean onDuty) {
        CourierProfile c = ownProfile(actor);
        if (onDuty) {
            c.goOnDuty();
            log("Courier " + c.getId() + " is now " + c.getStatus());
            manager.updateAvailability(c);               // observer: retry the dispatch queue
        } else {
            String offered = c.getPendingOfferId();
            c.goOffDuty();                               // throws first if carrying
            log("Courier " + c.getId() + " is now OFFLINE");
            if (offered != null) {                       // an unaccepted offer goes back to the queue
                c.clearOffer();
                dispatch.redispatch(offered);
            }
        }
        return view(c);
    }

    /** UC-DLV-03. Only the offered order, and only one order at a time. */
    public synchronized CourierView accept(Actor actor) {
        CourierProfile c = ownProfile(actor);
        String orderId = c.getPendingOfferId();
        c.accept();                                      // throws if no offer or not idle
        dispatch.clearHistory(orderId);
        log("Courier " + c.getId() + " accepted order " + orderId);
        return view(c);
    }

    /** Decline: the order goes to the next nearest courier (R-DLV-04). */
    public synchronized CourierView decline(Actor actor) {
        CourierProfile c = ownProfile(actor);
        String orderId = c.getPendingOfferId();
        if (orderId == null) throw new BusinessRuleException("No pending offer to decline");
        c.clearOffer();
        dispatch.decline(orderId, c.getId());
        manager.updateAvailability(c);
        return view(c);
    }

    /** UC-DLV-05. The order must be READY, so no transition is skipped (R-ORD-04). */
    public synchronized OrderResult confirmPickup(Actor actor) {
        CourierProfile c = ownProfile(actor);
        if (c.getStatus() != CourierStatus.ASSIGNED || c.getCurrentOrderId() == null) {
            throw new BusinessRuleException("No assigned order to pick up (" + c.getStatus() + ")");
        }
        Order order = findOrder(c.getCurrentOrderId());
        OrderStatus from = order.getStatus();
        order.pickUp();                                  // throws unless READY
        c.pickUp();
        return new OrderResult(order, record(order, from, actor));
    }

    /** UC-DLV-06. Closes the delivery, hands off to PRC, retries the dispatch queue. */
    public synchronized OrderResult confirmDelivery(Actor actor) {
        CourierProfile c = ownProfile(actor);
        if (c.getStatus() != CourierStatus.CARRYING || c.getCurrentOrderId() == null) {
            throw new BusinessRuleException("Not carrying an order (" + c.getStatus() + ")");
        }
        Order order = findOrder(c.getCurrentOrderId());
        OrderStatus from = order.getStatus();
        order.deliver();                                 // throws unless PICKED_UP
        c.deliver();
        OrderEvent event = record(order, from, actor);
        fares.orderDelivered(order);
        manager.updateAvailability(c);                   // observer: courier is idle again
        return new OrderResult(order, event);
    }

    // ---------- operator commands ----------

    /** UC-DLV-07. The only way an accepted assignment can change (R-DLV-02). */
    public synchronized Order reassign(Actor actor, String orderId) {
        requireRole(actor, Role.OPERATOR);
        Order order = findOrder(orderId);
        CourierProfile holder = couriers.findAll().stream()
                .filter(c -> orderId.equals(c.getCurrentOrderId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "Order " + orderId + " is not assigned to any courier"));
        if (holder.getStatus() != CourierStatus.OFFLINE) {
            throw new BusinessRuleException("Courier " + holder.getId() + " is "
                    + holder.getStatus() + ", not offline");
        }
        if (order.getStatus() != OrderStatus.READY) {
            throw new BusinessRuleException("Order " + orderId + " is already "
                    + order.getStatus());
        }
        holder.release();
        log("Operator " + actor.id() + " released order " + orderId
                + " from courier " + holder.getId());
        dispatch.redispatch(orderId);
        return order;
    }

    // ---------- queries ----------

    public CourierView myView(Actor actor) { return view(ownProfile(actor)); }

    public List<CourierView> listCouriers(Actor actor) {
        requireRole(actor, Role.OPERATOR);
        return couriers.findAll().stream().map(this::view).toList();
    }

    public List<String> getQueue(Actor actor) {
        requireRole(actor, Role.OPERATOR);
        return dispatch.getQueue();
    }

    /** Orders held by an offline courier and not yet picked up. */
    public List<Order> getStalled(Actor actor) {
        requireRole(actor, Role.OPERATOR);
        return couriers.findAll().stream()
                .filter(c -> c.getStatus() == CourierStatus.OFFLINE && c.getCurrentOrderId() != null)
                .map(c -> findOrder(c.getCurrentOrderId()))
                .toList();
    }

    // ---------- helpers ----------

    private CourierView view(CourierProfile c) {
        return new CourierView(c.getId(), c.getStatus().name(),
                c.getPendingOfferId(), bakeryOf(c.getPendingOfferId()),
                c.getCurrentOrderId(), bakeryOf(c.getCurrentOrderId()));
    }

    private String bakeryOf(String orderId) {
        if (orderId == null) return null;
        return orders.findById(orderId).map(Order::getBakeryId).orElse(null);
    }

    private CourierProfile ownProfile(Actor actor) {
        requireRole(actor, Role.COURIER);
        return couriers.findById(actor.id())
                .orElseThrow(() -> new NotFoundException("No courier profile for " + actor.id()));
    }

    private OrderEvent record(Order order, OrderStatus from, Actor actor) {
        OrderEvent event = EventLogger.getInstance()
                .record(order.getId(), from, order.getStatus(), actor.id());
        events.save(event);
        return event;
    }

    private void requireRole(Actor actor, Role required) {
        if (actor == null || actor.role() != required) {
            throw new ForbiddenException("Role " + (actor == null ? "none" : actor.role())
                    + " may not perform this action");
        }
    }

    private Order findOrder(String id) {
        return orders.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    private void log(String message) { System.out.println("[DELIVERY] " + message); }
}