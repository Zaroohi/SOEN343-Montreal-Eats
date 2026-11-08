package com.bakedtogo.business.view;

import com.bakedtogo.business.domain.Order;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bakery-facing: what to prepare. No prices. */
public class BakeryOrderView extends OrderView {
    public BakeryOrderView(OrderRenderer renderer) { super(renderer); }

    @Override
    public String display(Order o) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("order", o.getId());
        f.put("customer", o.getCustomerId());
        f.put("status", o.getStatus().name());
        f.put("prepare", o.getLines().stream()
                .map(l -> l.quantity() + " x " + l.name()).toList());
        if (o.getRejectionReason() != null) f.put("rejectionReason", o.getRejectionReason());
        return renderer.render("Incoming order", f);
    }
}