package com.bakedtogo.business.service;

import com.bakedtogo.business.dispatch.DispatchQueue;
import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.CustomerLocation;
import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.persistence.BakeryRepository;
import com.bakedtogo.persistence.CourierRepository;
import com.bakedtogo.persistence.CustomerLocationRepository;
import com.bakedtogo.persistence.OrderRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Builds the city map for the dashboard, scoped by role. */
@Service
public class MapService {

    private final BakeryRepository bakeries;
    private final CourierRepository couriers;
    private final OrderRepository orders;
    private final CustomerLocationRepository customers;
    private final DispatchQueue queue;

    public MapService(BakeryRepository bakeries, CourierRepository couriers,
                      OrderRepository orders, CustomerLocationRepository customers,
                      DispatchQueue queue) {
        this.bakeries = bakeries;
        this.couriers = couriers;
        this.orders = orders;
        this.customers = customers;
        this.queue = queue;
    }

    public MapView view(Actor actor) {
        if (actor == null) throw new ForbiddenException("No authenticated user");
        return switch (actor.role()) {
            case OPERATOR -> new MapView(
                    bakeries.findAll().stream().map(this::bakeryPoint).toList(),
                    couriers.findAll().stream().map(this::courierPoint).toList(),
                    customers.findAll().stream().map(this::customerPoint).toList(),
                    queue.getPending());
            case BAKERY_STAFF -> {
                if (actor.bakeryId() == null) throw new ForbiddenException("Staff without a bakery");
                List<Order> relevant = orders.findByBakeryId(actor.bakeryId());
                Set<String> customerIds = new HashSet<>();
                for (Order o : relevant) customerIds.add(o.getCustomerId());
                yield scoped(relevant, customerIds,
                        bakeries.findById(actor.bakeryId()).stream().toList());
            }
            case CUSTOMER -> scoped(orders.findByCustomerId(actor.id()),
                    Set.of(actor.id()), bakeries.findAll());
            case COURIER -> courierScoped(actor);
        };
    }

    private MapView scoped(List<Order> relevant, Set<String> customerIds, List<Bakery> visible) {
        Set<String> orderIds = new HashSet<>();
        for (Order o : relevant) orderIds.add(o.getId());
        return new MapView(
                visible.stream().map(this::bakeryPoint).toList(),
                couriers.findAll().stream()
                        .filter(c -> orderOf(c) != null && orderIds.contains(orderOf(c)))
                        .map(this::courierPoint).toList(),
                customerIds.stream().map(customers::findById).flatMap(Optional::stream)
                        .map(this::customerPoint).toList(),
                List.of());
    }

    private MapView courierScoped(Actor actor) {
        CourierProfile me = couriers.findById(actor.id()).orElse(null);
        if (me == null) return new MapView(List.of(), List.of(), List.of(), List.of());
        String orderId = orderOf(me);
        Order o = orderId == null ? null : orders.findById(orderId).orElse(null);
        List<MapView.BakeryPoint> bp = o == null ? List.of()
                : bakeries.findById(o.getBakeryId())
                        .map(b -> List.of(bakeryPoint(b))).orElse(List.of());
        List<MapView.CustomerPoint> cp = (o != null && me.getStatus() == CourierStatus.CARRYING)
                ? customers.findById(o.getCustomerId())
                        .map(c -> List.of(customerPoint(c))).orElse(List.of())
                : List.of();
        return new MapView(bp, List.of(courierPoint(me)), cp, List.of());
    }

    private String orderOf(CourierProfile c) {
        return c.getCurrentOrderId() != null ? c.getCurrentOrderId() : c.getPendingOfferId();
    }

    private MapView.BakeryPoint bakeryPoint(Bakery b) {
        return new MapView.BakeryPoint(b.getId(), b.getName(), b.getStatus().name(),
                b.getLatitude(), b.getLongitude(), b.getPreparationQueue().size());
    }

    private MapView.CustomerPoint customerPoint(CustomerLocation c) {
        return new MapView.CustomerPoint(c.id(), c.name(), c.latitude(), c.longitude());
    }

    private MapView.CourierPoint courierPoint(CourierProfile c) {
        String orderId = orderOf(c);
        Order o = orderId == null ? null : orders.findById(orderId).orElse(null);
        String type = null;
        Double tLat = null, tLon = null;
        if (o != null && c.getStatus() != CourierStatus.OFFLINE) {
            if (c.getStatus() == CourierStatus.CARRYING) {
                CustomerLocation loc = customers.findById(o.getCustomerId()).orElse(null);
                if (loc != null) { type = "delivery"; tLat = loc.latitude(); tLon = loc.longitude(); }
            } else {
                Bakery b = bakeries.findById(o.getBakeryId()).orElse(null);
                if (b != null) {
                    type = c.getCurrentOrderId() != null ? "pickup" : "offer";
                    tLat = b.getLatitude();
                    tLon = b.getLongitude();
                }
            }
        }
        return new MapView.CourierPoint(c.getId(), c.getStatus().name(),
                c.getLatitude(), c.getLongitude(), orderId, type, tLat, tLon,
                c.legElapsedMillis());
    }
}