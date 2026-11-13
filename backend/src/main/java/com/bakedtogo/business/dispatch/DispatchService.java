package com.bakedtogo.business.dispatch;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.persistence.BakeryRepository;
import com.bakedtogo.persistence.CourierRepository;
import com.bakedtogo.persistence.OrderRepository;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import com.bakedtogo.business.reset.Resettable;

/** UC-DLV-02 (dispatch rule) and UC-DLV-04 (queue). */
@Service
public class DispatchService implements Resettable {

    private final CourierRepository couriers;
    private final BakeryRepository bakeries;
    private final OrderRepository orders;
    private final DispatchQueue queue;
    private final CourierManager manager;
    /** Couriers who declined an order are not offered it again (used by decline in batch 4). */
    private final Map<String, Set<String>> declinedBy = new ConcurrentHashMap<>();

    public DispatchService(CourierRepository couriers, BakeryRepository bakeries,
                           OrderRepository orders, DispatchQueue queue, CourierManager manager) {
        this.couriers = couriers;
        this.bakeries = bakeries;
        this.orders = orders;
        this.queue = queue;
        this.manager = manager;
        queue.setRetryHandler(this::retryQueue);
        manager.subscribe(queue);
    }

    /** Entry point when a bakery marks an order READY. */
    public synchronized void dispatch(String orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.READY) {          // R-DLV-01
            throw new BusinessRuleException("Order " + orderId + " is not ready ("
                    + order.getStatus() + "), it cannot be dispatched");
        }
        if (!tryOffer(order)) {
            queue.enqueue(orderId);
            log("Order " + orderId + " is waiting for a courier");
        }
    }

    /** Re-offers a READY order (reassignment or a released offer). Queues it if nobody is free. */
    public synchronized void redispatch(String orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.READY) {
            throw new BusinessRuleException("Order " + orderId + " is not ready ("
                    + order.getStatus() + ")");
        }
        if (!tryOffer(order)) {
            queue.enqueue(orderId);
            log("Order " + orderId + " is waiting for a courier");
        }
    }

    /** A courier declined: exclude them for this order and offer it to the next nearest. */
    public synchronized void decline(String orderId, String courierId) {
        declinedBy.computeIfAbsent(orderId, k -> ConcurrentHashMap.newKeySet()).add(courierId);
        log("Courier " + courierId + " declined order " + orderId);
        redispatch(orderId);
    }

    public void clearHistory(String orderId) { declinedBy.remove(orderId); }

    /** Called through the observer when a courier becomes available. Oldest order first. */
    private synchronized void retryQueue(CourierProfile courier) {
        for (String orderId : queue.getPending()) {
            Order order = findOrder(orderId);
            if (order.getStatus() == OrderStatus.READY && tryOffer(order)) {
                queue.remove(orderId);
            }
        }
    }

    /** Dispatch rule: nearest available courier, ties broken by lowest id. */
    private boolean tryOffer(Order order) {
        Bakery bakery = bakeries.findById(order.getBakeryId())
                .orElseThrow(() -> new NotFoundException("Bakery " + order.getBakeryId() + " not found"));
        Set<String> excluded = declinedBy.getOrDefault(order.getId(), Set.of());

        Optional<CourierProfile> nearest = couriers.findAll().stream()
                .filter(CourierProfile::isAvailable)
                .filter(c -> !excluded.contains(c.getId()))
                .min(Comparator
                        .comparingDouble((CourierProfile c) ->
                                c.distanceTo(bakery.getLatitude(), bakery.getLongitude()))
                        .thenComparing(CourierProfile::getId));

        if (nearest.isEmpty()) return false;
        CourierProfile courier = nearest.get();
        courier.offer(order.getId());
        log("Order " + order.getId() + " offered to courier " + courier.getId());
        return true;
    }

    public java.util.List<String> getQueue() { return queue.getPending(); }

    private Order findOrder(String id) {
        return orders.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    private void log(String message) { System.out.println("[DISPATCH] " + message); }

    @Override 
    public synchronized void reset() { declinedBy.clear(); }
}