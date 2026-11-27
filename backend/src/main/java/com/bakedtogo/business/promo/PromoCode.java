package com.bakedtogo.business.promo;

import java.time.LocalDate;

/** One promotion rule from promos.json. Valid through the end of expiresOn. */
public record PromoCode(String code, Type type, double value,
                        double minOrderValue, LocalDate expiresOn) {

    public enum Type { PERCENT, FIXED }

    public boolean isExpiredOn(LocalDate today) {
        return expiresOn != null && today.isAfter(expiresOn);
    }

    public double discountFor(double subtotal) {
        return type == Type.PERCENT ? subtotal * value / 100.0 : value;
    }
}