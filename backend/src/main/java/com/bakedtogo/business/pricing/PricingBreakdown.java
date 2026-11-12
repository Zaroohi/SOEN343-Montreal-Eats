package com.bakedtogo.business.pricing;

public record PricingBreakdown (
    double subtotal,
    double discount,
    double deliveryFee,
    double serviceFee,
    double smallOrderSurcharge,
    double total

){}
