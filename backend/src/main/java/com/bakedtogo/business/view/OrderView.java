package com.bakedtogo.business.view;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.pricing.PricingBreakdown;

/** Abstraction side of the Bridge: decides WHAT to show, delegates HOW to the renderer. */
public abstract class OrderView {
    protected final OrderRenderer renderer;

    protected OrderView(OrderRenderer renderer) { this.renderer = renderer; }

    public abstract String display(Order order);
}