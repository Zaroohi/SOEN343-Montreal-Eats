package com.bakedtogo.presentation.dto;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.service.OrderResult;
import java.util.List;

public record OrderResponse(String orderId, String bakeryId, String customerId,
                            String status, List<OrderLine> lines, double total,
                            String rejectionReason, String eventId) {

    public static OrderResponse from(OrderResult r) {
        return from(r.order(), r.event().eventId());
    }

    public static OrderResponse from(Order o, String eventId) {
        return new OrderResponse(o.getId(), o.getBakeryId(), o.getCustomerId(),
                o.getStatus().name(), o.getLines(), o.getTotal(),
                o.getRejectionReason(), eventId);
    }
}