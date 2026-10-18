package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderEvent;

/** What every command returns, so the console can show the new state and its event id. */
public record OrderResult(Order order, OrderEvent event) {}