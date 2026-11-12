package com.bakedtogo.business.pricing;

import com.bakedtogo.business.domain.OrderLine;
import java.util.List;
import com.bakedtogo.infrastructure.PricingSettings;

/** Regular price minus a percentage discount. */
public class PromotionalPricing implements PricingStrategy {
   private final double discount;
private final PricingSettings settings;

public PromotionalPricing() {
    this(0.10, new PricingSettings(0, 0, 0, 0));
}

public PromotionalPricing(double discount) {
    this(discount, new PricingSettings(0, 0, 0, 0));
}

public PromotionalPricing(double discount, PricingSettings settings) {
    if (settings == null) {
        throw new IllegalArgumentException("Pricing settings cannot be null");
    }
    this.discount = discount;
    this.settings = settings;
}

    public String name() { return "promo"; }

   
@Override
public double calculate(List<OrderLine> lines) {
    return calculateBreakdown(lines).total();
}

public PricingBreakdown calculateBreakdown(List<OrderLine> lines) {
    double subtotal = lines.stream()
            .mapToDouble(l -> l.unitPrice() * l.quantity())
            .sum();

    double discountAmount = subtotal * discount;
    double discountedSubtotal = subtotal - discountAmount;

    double surcharge = 0.0;
    if (subtotal < settings.smallOrderThreshold()) {
        surcharge = settings.smallOrderSurcharge();
    }

    double total = discountedSubtotal
            + settings.deliveryFee()
            + settings.serviceFee()
            + surcharge;

    total = Math.round(total * 100) / 100.0;

    return new PricingBreakdown(
            subtotal,
            discountAmount,
            settings.deliveryFee(),
            settings.serviceFee(),
            surcharge,
            total
    );
}

}