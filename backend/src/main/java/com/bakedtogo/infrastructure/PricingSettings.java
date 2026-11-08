
package com.bakedtogo.infrastructure;

public record PricingSettings(
    double deliveryFee,
    double serviceFee,
    double smallOrderThreshold,
    double smallOrderSurcharge
) {
}
