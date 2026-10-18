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
    public PricingStrategy pricingStrategy(@Value("${pricing.strategy:regular}") String name) {
        return "promo".equalsIgnoreCase(name) ? new PromotionalPricing() : new RegularPricing();
    }
}