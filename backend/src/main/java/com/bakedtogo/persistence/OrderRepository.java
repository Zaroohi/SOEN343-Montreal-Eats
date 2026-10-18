package com.bakedtogo.persistence;

import com.bakedtogo.business.domain.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(String id);
    List<Order> findAll();
    List<Order> findByCustomerId(String customerId);
    List<Order> findByBakeryId(String bakeryId);
    void save(Order order);
}