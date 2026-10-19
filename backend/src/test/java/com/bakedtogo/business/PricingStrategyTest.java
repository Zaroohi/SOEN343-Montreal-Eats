// test/.../business/PricingStrategyTest.java
package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.pricing.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingStrategyTest {
    private final List<OrderLine> lines = List.of(new OrderLine("m1", "Croissant", 3.50, 2));

    @Test void regularIsTheSum() {
        assertEquals(7.00, new RegularPricing().calculate(lines));
    }

    @Test void promoAppliesDiscount() {
        assertEquals(6.30, new PromotionalPricing().calculate(lines));
    }

    @Test void strategiesAreInterchangeable() {
        PricingStrategy s = new RegularPricing();
        assertNotEquals(s.calculate(lines), new PromotionalPricing().calculate(lines));
    }
}