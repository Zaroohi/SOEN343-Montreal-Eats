package com.bakedtogo.presentation.dto;

import com.bakedtogo.business.domain.Bakery;

public record BakeryResponse(String id, String name, String status, int queueLength) {
    public static BakeryResponse from(Bakery b) {
        return new BakeryResponse(b.getId(), b.getName(), b.getStatus().name(),
                b.getPreparationQueue().size());
    }
}