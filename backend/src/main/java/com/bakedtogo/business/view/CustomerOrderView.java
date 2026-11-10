package com.bakedtogo.business.view;

import com.bakedtogo.business.domain.Order;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import com.bakedtogo.business.pricing.PricingBreakdown;

/** Customer-facing: status, items with prices, and the total. */
public class CustomerOrderView extends OrderView {
    public CustomerOrderView(OrderRenderer renderer)
     { super(renderer); 
        
     }

    @Override
    public String display(Order o) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("order", o.getId());
        f.put("bakery", o.getBakeryId());
        f.put("status", o.getStatus().name());
        f.put("items", o.getLines().stream()
                .map(l -> String.format(Locale.ROOT, "%d x %s @ %.2f",
                        l.quantity(), l.name(), l.unitPrice()))
                .toList());
        
PricingBreakdown breakdown = o.getPricingBreakdown();
System.out.println("Debug: " + breakdown);

if (breakdown != null) {
    f.put("subtotal", breakdown.subtotal());
    f.put("discount", breakdown.discount());
    f.put("deliveryFee", breakdown.deliveryFee());
    f.put("serviceFee", breakdown.serviceFee());
    f.put("smallOrderSurcharge", breakdown.smallOrderSurcharge());
}

        f.put("total", o.getTotal());
        if (o.getRejectionReason() != null) f.put("rejectionReason", o.getRejectionReason());
        return renderer.render("Your order", f);
    }
}