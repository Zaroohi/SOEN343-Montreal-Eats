package com.bakedtogo.persistence.memory;

import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.persistence.OrderEventRepository;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderEventRepository implements OrderEventRepository {
    private final List<OrderEvent> events = new CopyOnWriteArrayList<>();

    public void save(OrderEvent event) { events.add(event); }
    public List<OrderEvent> findByOrderId(String orderId) {
        return events.stream().filter(e -> e.orderId().equals(orderId)).toList();
    }
    public List<OrderEvent> findAll() { return List.copyOf(events); }
}