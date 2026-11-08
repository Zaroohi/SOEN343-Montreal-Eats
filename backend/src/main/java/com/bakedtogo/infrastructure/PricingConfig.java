package com.bakedtogo.infrastructure;

import com.bakedtogo.business.pricing.PricingStrategy;
import com.bakedtogo.business.pricing.PromotionalPricing;
import com.bakedtogo.business.pricing.RegularPricing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Chooses the active strategy from application.properties (pricing.strategy=regular|promo). */
@Configuration
public class PricingConfig {
    
@Bean
public PricingStrategy pricingStrategy(
        @Value("${pricing.strategy:regular}") String name,
        PricingSettings settings) {

    return "promo".equalsIgnoreCase(name)
           ? new PromotionalPricing(0.10, settings)
            : new RegularPricing(settings);
}

 // To read the pricing settings from app.properties    
    @Bean
    public PricingSettings pricingSettings(
        @Value("${pricing.delivery-fee}") double deliveryFee,
        @Value("${pricing.service-fee}") double serviceFee,
        @Value("${pricing.small-order-threshold}") double smallOrderThreshold,
        @Value("${pricing.small-order-surcharge}") double smallOrderSurcharge) {

    return new PricingSettings(
        deliveryFee,
        serviceFee,
        smallOrderThreshold,
        smallOrderSurcharge
    );
}
}