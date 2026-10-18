package com.bakedtogo.business.pricing;

import com.bakedtogo.business.domain.OrderLine;
import java.util.List;

public class RegularPricing implements PricingStrategy {
    public String name() { return "regular"; }

    public double calculate(List<OrderLine> lines) {
        double sum = lines.stream().mapToDouble(l -> l.unitPrice() * l.quantity()).sum();
        return Math.round(sum * 100) / 100.0;
    }
}