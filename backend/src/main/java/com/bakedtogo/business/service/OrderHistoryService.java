
package com.bakedtogo.business.service;

import com.bakedtogo.business.billing.FareLedger;
import com.bakedtogo.business.billing.LedgerEntry;
import com.bakedtogo.business.billing.LedgerService;
import com.bakedtogo.business.billing.OrderHistoryResponse;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.enums.Role;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderHistoryService {

    private final FareLedger ledger;
    private final LedgerService ledgerService;

    public OrderHistoryService(FareLedger ledger, LedgerService ledgerService) {
        this.ledger = ledger;
        this.ledgerService = ledgerService;
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
            .sorted(Comparator.comparing(LedgerEntry::createdAt).reversed()).map(entry -> {
                boolean refunded = ledgerService
                    .findByOrderId(entry.orderId()).stream().anyMatch(transaction ->
                        transaction.type().equals("REFUND")
                    );

                return new OrderHistoryResponse(
                    entry.orderId(),
                    refunded
                        ? OrderStatus.REFUNDED
                        : entry.finalState(),
                    entry.createdAt(),
                    entry.deliveredAt(),
                    actor.role() == Role.COURIER
                        ? null
                        : entry.totalCharge()
                );
            })
            .toList();
    }
}
