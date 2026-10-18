package com.bakedtogo.presentation.dto;
import java.util.List;
public record PlaceOrderRequest(String bakeryId, List<LineRequest> lines) {}