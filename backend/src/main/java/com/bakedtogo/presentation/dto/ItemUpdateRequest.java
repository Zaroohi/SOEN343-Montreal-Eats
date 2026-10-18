package com.bakedtogo.presentation.dto;
import com.bakedtogo.business.domain.enums.ItemStatus;
public record ItemUpdateRequest(String name, Double price, ItemStatus status) {}