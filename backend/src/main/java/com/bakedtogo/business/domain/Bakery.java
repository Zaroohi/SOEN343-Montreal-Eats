package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Bakery {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private BakeryStatus status;
    private final List<MenuItem> menu = new ArrayList<>();
    private final Deque<Order> preparationQueue = new ArrayDeque<>();

    public Bakery(String id, String name, double latitude, double longitude, BakeryStatus status) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public BakeryStatus getStatus() { return status; }
    public void setStatus(BakeryStatus status) { this.status = status; }
    public List<MenuItem> getMenu() { return menu; }

    public void addMenuItem(MenuItem item) { menu.add(item); }

    /** Accepted orders enter at the tail (arrival order, R-ORD-03). */
    public void enqueue(Order order) { preparationQueue.addLast(order); }

    /** Only the head of the queue may start preparing: no order is prepared out of turn. */
    public void checkIsNext(Order order) {
        Order head = preparationQueue.peekFirst();
        if (head == null || !head.getId().equals(order.getId())) {
            throw new BusinessRuleException("Order " + order.getId()
                    + " is not next in the queue (next is "
                    + (head == null ? "none" : head.getId()) + ")");
        }
    }

    /** Removes the order from the queue once it is ready. */
    public void dequeue(Order order) { preparationQueue.remove(order); }

    public List<Order> getPreparationQueue() { return List.copyOf(preparationQueue); }
}