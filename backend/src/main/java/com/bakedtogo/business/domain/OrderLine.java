package com.bakedtogo.business.domain;

import com.bakedtogo.business.domain.exception.BusinessRuleException;

public record OrderLine(String menuItemId, String name, double unitPrice, int quantity) {
    public OrderLine {
        if (quantity <= 0) {
            throw new BusinessRuleException("Quantity must be positive for item " + menuItemId);
        }
    }
}