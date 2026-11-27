package com.bakedtogo.business.promo;

import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.pricing.PricingBreakdown;
import com.bakedtogo.infrastructure.PromoLoader;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** UC-PROMO-01 (apply), UC-PROMO-02 (reject). R-PROMO-01 / R-PROMO-02. */
@Service
public class PromoService {

    private final Map<String, PromoCode> rules;
    private final Clock clock;

    @Autowired
    public PromoService() {
        this(new PromoLoader().load(), Clock.systemDefaultZone());
    }

    public PromoService(Map<String, PromoCode> rules, Clock clock) {
        this.rules = rules;
        this.clock = clock;
    }

    public record Applied(String code, PricingBreakdown breakdown) {}

    /** Returns null if no code was entered. Throws BusinessRuleException with the reason if refused. */
    public Applied apply(String rawCode, PricingBreakdown base) {
        if (rawCode == null || rawCode.isBlank()) return null;
        String code = rawCode.trim();
        try {
            code = normalise(rawCode);
            PromoCode rule = rules.get(code);
            if (rule == null) throw new BusinessRuleException("Promo code " + code + " is not valid");
            LocalDate today = LocalDate.now(clock);
            if (rule.isExpiredOn(today)) {
                throw new BusinessRuleException("Promo code " + code + " expired on " + rule.expiresOn());
            }
            if (base.subtotal() < rule.minOrderValue()) {
                throw new BusinessRuleException(String.format(Locale.ROOT,
                        "Promo code %s needs a minimum order of $%.2f (your items total $%.2f)",
                        code, rule.minOrderValue(), base.subtotal()));
            }
            Applied applied = new Applied(code, discounted(base, rule));
            System.out.println("[PROMO] " + code + " applied: -$"
                    + String.format(Locale.ROOT, "%.2f", applied.breakdown().discount() - base.discount()));
            return applied;
        } catch (BusinessRuleException e) {
            System.out.println("[PROMO] refused: " + e.getMessage());
            throw e;
        }
    }

    private PricingBreakdown discounted(PricingBreakdown base, PromoCode rule) {
        double room = Math.max(0.0, base.subtotal() - base.discount());
        double promo = round(Math.min(rule.discountFor(base.subtotal()), room));
        double total = round(base.total() - promo);
        return new PricingBreakdown(base.subtotal(), round(base.discount() + promo),
                base.deliveryFee(), base.serviceFee(), base.smallOrderSurcharge(), total);
    }

    private String normalise(String raw) {
        String code = raw.trim();
        if (code.matches(".*[\\s,;+&].*")) {
            throw new BusinessRuleException("Only one promo code can be used per order");
        }
        return code.toUpperCase(Locale.ROOT);
    }

    private static double round(double v) { return Math.round(v * 100) / 100.0; }
}