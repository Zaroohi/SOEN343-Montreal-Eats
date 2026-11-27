package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.enums.*;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.pricing.RegularPricing;
import com.bakedtogo.business.promo.PromoCode;
import com.bakedtogo.business.promo.PromoService;
import com.bakedtogo.business.service.*;
import com.bakedtogo.infrastructure.PricingSettings;
import com.bakedtogo.persistence.memory.*;
import java.time.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromoTest {

    private OrderService service;
    private final Actor customer = new Actor("c1", Role.CUSTOMER, null);

    @BeforeEach
    void setUp() {
        InMemoryBakeryRepository bakeries = new InMemoryBakeryRepository();
        Bakery b = new Bakery("b1", "Open", 0, 0, BakeryStatus.OPEN);
        b.addMenuItem(new MenuItem("m1", "Croissant", 3.5, ItemStatus.AVAILABLE));
        b.addMenuItem(new MenuItem("m5", "Cake", 20.0, ItemStatus.AVAILABLE));
        bakeries.save(b);
        service = new OrderService(new InMemoryOrderRepository(), new InMemoryOrderEventRepository(),
                bakeries, new RegularPricing(new PricingSettings(3.00, 1.50, 15.00, 2.00)), null);
        Clock clock = Clock.fixed(Instant.parse("2026-11-27T12:00:00Z"), ZoneOffset.UTC);
        service.setPromoService(new PromoService(Map.of(
                "TEN", new PromoCode("TEN", PromoCode.Type.PERCENT, 10, 10.0, LocalDate.of(2027, 1, 1)),
                "FIVE", new PromoCode("FIVE", PromoCode.Type.FIXED, 5, 0.0, LocalDate.of(2027, 1, 1)),
                "OLD", new PromoCode("OLD", PromoCode.Type.PERCENT, 20, 0.0, LocalDate.of(2026, 8, 31)),
                "BIG", new PromoCode("BIG", PromoCode.Type.PERCENT, 15, 50.0, LocalDate.of(2027, 1, 1))), clock));
    }

    private List<OrderItemRequest> cakes(int n) { return List.of(new OrderItemRequest("m5", n)); }

    @Test void noCodeKeepsRegularFare() {
        var o = service.placeOrder(customer, "b1", cakes(1), null).order();
        assertEquals(24.50, o.getTotal(), 0.001);
        assertNull(o.getPromoCode());
    }

    @Test void percentCodeDiscountsTheItemSubtotal() {
        var o = service.placeOrder(customer, "b1", cakes(1), "ten").order();
        assertEquals("TEN", o.getPromoCode());
        assertEquals(2.00, o.getPricingBreakdown().discount(), 0.001);
        assertEquals(22.50, o.getTotal(), 0.001);
    }

    @Test void fixedCodeIsCappedAtTheSubtotal() {
        var o = service.placeOrder(customer, "b1", List.of(new OrderItemRequest("m1", 1)), "FIVE").order();
        assertEquals(3.50, o.getPricingBreakdown().discount(), 0.001);
        assertEquals(6.50, o.getTotal(), 0.001);
    }

    @Test void expiredCodeIsRefusedWithReasonAndNothingIsSaved() {
        var ex = assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b1", cakes(1), "OLD"));
        assertTrue(ex.getMessage().contains("expired"));
        assertTrue(service.getOrders(customer).isEmpty());
    }

    @Test void codeBelowMinimumIsRefused() {
        var ex = assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b1", cakes(1), "BIG"));
        assertTrue(ex.getMessage().contains("minimum"));
    }

    @Test void unknownCodeIsRefused() {
        assertThrows(BusinessRuleException.class, () -> service.placeOrder(customer, "b1", cakes(1), "NOPE"));
    }

    @Test void onlyOneCodePerOrder() {
        var ex = assertThrows(BusinessRuleException.class,
                () -> service.placeOrder(customer, "b1", cakes(1), "TEN, FIVE"));
        assertTrue(ex.getMessage().contains("one promo code"));
    }
}