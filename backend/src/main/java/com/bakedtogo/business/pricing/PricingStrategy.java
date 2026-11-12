package com.bakedtogo.business.pricing;

import com.bakedtogo.business.domain.OrderLine;
import java.util.List;

/** Strategy interface: how an order total is computed. */
public interface PricingStrategy {
    String name();
    double calculate(List<OrderLine> lines);
    PricingBreakdown calculateBreakdown(List<OrderLine> lines);
}