package com.bakedtogo.persistence.memory;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.persistence.OrderRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public Optional<Order> findById(String id) { return Optional.ofNullable(store.get(id)); }
    public List<Order> findAll() { return List.copyOf(store.values()); }
    public List<Order> findByCustomerId(String customerId) {
        return store.values().stream().filter(o -> o.getCustomerId().equals(customerId)).toList();
    }
    public List<Order> findByBakeryId(String bakeryId) {
        return store.values().stream().filter(o -> o.getBakeryId().equals(bakeryId)).toList();
    }
    public void save(Order order) { store.put(order.getId(), order); }
}