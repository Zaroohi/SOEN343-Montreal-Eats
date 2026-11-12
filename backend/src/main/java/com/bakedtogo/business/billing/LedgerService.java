package com.bakedtogo.business.billing;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LedgerService {

    private final LedgerEntryFactory chargeFactory =
            new ChargeEntryFactory();

    private final LedgerEntryFactory refundFactory =
            new RefundEntryFactory();

    private final List<LedgerTransaction> transactions =
            new CopyOnWriteArrayList<>();

    public synchronized LedgerTransaction recordCharge(
            String orderId, BigDecimal amount) {

        boolean alreadyCharged = transactions.stream()
                .anyMatch(t -> t.orderId().equals(orderId)
                        && t.type().equals("CHARGE"));

        if (alreadyCharged) {
            throw new IllegalStateException(
                    "Charge already recorded for this order");
        }

        LedgerTransaction entry =
                chargeFactory.createEntry(orderId, amount);

        transactions.add(entry);
        return entry;
    }

    public synchronized LedgerTransaction recordRefund(
            String orderId, BigDecimal amount) {

        boolean alreadyRefunded = transactions.stream()
                .anyMatch(t -> t.orderId().equals(orderId)
                        && t.type().equals("REFUND"));

        if (alreadyRefunded) {
            throw new IllegalStateException(
                    "Order has already been refunded");
        }

        LedgerTransaction entry =
                refundFactory.createEntry(orderId, amount);

        transactions.add(entry);
        return entry;
    }

    public List<LedgerTransaction> findByOrderId(String orderId) {
        return transactions.stream()
                .filter(t -> t.orderId().equals(orderId))
                .toList();
    }

    public List<LedgerTransaction> findAll() {
        return List.copyOf(transactions);
    }

public synchronized void rollbackRefund(String orderId) {
    transactions.removeIf(t ->
        t.orderId().equals(orderId)
        && t.type().equals("REFUND")
    );
}


}
