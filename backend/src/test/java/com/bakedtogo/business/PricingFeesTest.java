
package com.bakedtogo.business; 

import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.infrastructure.PricingSettings;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.pricing.PromotionalPricing;


public class PricingFeesTest {
      @Test
    void shouldApplySmallOrderSurcharge() {
        PricingSettings settings = new PricingSettings(
                3.00, 1.50, 15.00, 2.00
        );

        RegularPricing pricing = new RegularPricing(settings);

        double total = pricing.calculate(List.of());

        assertEquals(6.50, total, 0.001);
    }
    

@Test
void shouldNotApplySurchargeAboveThreshold() {
    PricingSettings settings = new PricingSettings(
            3.00, 1.50, 15.00, 2.00
    );

    RegularPricing pricing = new RegularPricing(settings);

    OrderLine item = new OrderLine(
            "item1", "Chocolate Cake", 20.00, 1
    );

    double total = pricing.calculate(List.of(item));

    assertEquals(24.50, total, 0.001);
}

@Test
void shouldNotApplySurchargeAtThreshold() {
    PricingSettings settings = new PricingSettings(
            3.00, 1.50, 15.00, 2.00
    );

    RegularPricing pricing = new RegularPricing(settings);

    OrderLine item = new OrderLine(
            "item2", "Pastry Box", 15.00, 1
    );

    double total = pricing.calculate(List.of(item));

    assertEquals(19.50, total, 0.001);
}
@Test
void shouldApplyPromotionalDiscountAndFees() {
    PricingSettings settings = new PricingSettings(3.00, 1.50, 15.00, 2.00);
    PromotionalPricing pricing = new PromotionalPricing(0.10, settings);

    OrderLine item = new OrderLine("item3", "Chocolate Cake", 20.00, 1);

    double total = pricing.calculate(List.of(item));

    assertEquals(22.50, total, 0.001);
}
    
}
