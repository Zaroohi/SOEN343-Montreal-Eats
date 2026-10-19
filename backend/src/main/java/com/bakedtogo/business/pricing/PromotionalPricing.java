package com.bakedtogo.business.pricing;

import com.bakedtogo.business.domain.OrderLine;
import java.util.List;

/** Regular price minus a percentage discount. */
public class PromotionalPricing implements PricingStrategy {
    private final double discount;

    public PromotionalPricing() { this(0.10); }
    public PromotionalPricing(double discount) { this.discount = discount; }

    public String name() { return "promo"; }

    public double calculate(List<OrderLine> lines) {
        double sum = lines.stream().mapToDouble(l -> l.unitPrice() * l.quantity()).sum();
        return Math.round(sum * (1 - discount) * 100) / 100.0;
    }
}