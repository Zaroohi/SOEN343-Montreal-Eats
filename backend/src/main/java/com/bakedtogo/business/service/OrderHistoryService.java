
package com.bakedtogo.business.service;

import com.bakedtogo.business.billing.FareLedger;
import com.bakedtogo.business.billing.LedgerEntry;
import org.springframework.stereotype.Service;
import com.bakedtogo.business.billing.OrderHistoryResponse;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderHistoryService {

    private final FareLedger ledger;

    public OrderHistoryService(FareLedger ledger) {
        this.ledger = ledger;
    }

public List<OrderHistoryResponse> getHistory(Actor actor) {
    if (actor == null || actor.role() == null) {
        throw new SecurityException("Authentication required");
    }

    return ledger.findAll().stream()
        .filter(entry -> switch (actor.role()) {
            case CUSTOMER ->
                actor.id().equals(entry.customerId());

            case BAKERY_STAFF ->
                actor.bakeryId() != null
                && actor.bakeryId().equals(entry.bakeryId());

            case COURIER ->
                actor.id().equals(entry.courierId());

            case OPERATOR -> true;
        })
        .sorted(Comparator.comparing(
            LedgerEntry::createdAt
        ).reversed())
        .map(entry -> new OrderHistoryResponse(
            entry.orderId(),
            entry.finalState(),
            entry.createdAt(),
            entry.deliveredAt(),
            actor.role() == com.bakedtogo.business.domain.enums.Role.COURIER
                ? null
                : entry.totalCharge()
        ))
        .toList();
}

}
