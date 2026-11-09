package com.bakedtogo.business.pricing;

import com.bakedtogo.infrastructure.PricingSettings;
import com.bakedtogo.business.domain.OrderLine;
import java.util.List;

public class RegularPricing implements PricingStrategy {
  private final PricingSettings settings;

  
public RegularPricing() {
    this(new PricingSettings(0, 0, 0, 0));
}

public RegularPricing(PricingSettings settings) {
    if (settings == null) {
        throw new IllegalArgumentException("Pricing settings cannot be null");
    }
    this.settings = settings;
}
    public String name() { return "regular"; }

        
public double calculate(List<OrderLine> lines) {
    double subtotal = lines.stream()
            .mapToDouble(l -> l.unitPrice() * l.quantity())
            .sum();

    double surcharge = 0.0;

    if (subtotal < settings.smallOrderThreshold()) {
        surcharge = settings.smallOrderSurcharge();
    }

    double total = subtotal  + settings.deliveryFee() + settings.serviceFee() + surcharge;
    return Math.round(total * 100) / 100.0;
}
}

