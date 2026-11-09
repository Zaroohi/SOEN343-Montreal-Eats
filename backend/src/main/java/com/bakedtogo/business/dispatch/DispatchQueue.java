package com.bakedtogo.business.dispatch;

import com.bakedtogo.business.domain.CourierProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/** Observer: FIFO queue of READY orders with no courier. Retries when a courier frees up. */
@Component
public class DispatchQueue implements CourierObserver {
    private final ConcurrentLinkedDeque<String> pendingOrders = new ConcurrentLinkedDeque<>();
    private Consumer<CourierProfile> retryHandler = c -> {};

    /** DispatchService registers the code that applies the dispatch rule. */
    public void setRetryHandler(Consumer<CourierProfile> handler) { this.retryHandler = handler; }

    public void enqueue(String orderId) {
        if (!pendingOrders.contains(orderId)) pendingOrders.addLast(orderId);
    }

    public void remove(String orderId) { pendingOrders.remove(orderId); }

    /** Oldest first. */
    public List<String> getPending() { return new ArrayList<>(pendingOrders); }

    @Override
    public void update(CourierProfile courier) { retryAssignment(courier); }

    public void retryAssignment(CourierProfile courier) { retryHandler.accept(courier); }
}