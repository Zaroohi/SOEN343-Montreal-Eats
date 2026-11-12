package com.bakedtogo.persistence;

import com.bakedtogo.business.domain.OrderEvent;
import java.util.List;

public interface OrderEventRepository {
    void save(OrderEvent event);
    List<OrderEvent> findByOrderId(String orderId);
    List<OrderEvent> findAll();

    // Remove only the specified event during rollback
    void delete(OrderEvent event);
}
