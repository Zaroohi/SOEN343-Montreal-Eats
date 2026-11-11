
package com.bakedtogo.business.billing;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FareLedger {

    private final Map<String, LedgerEntry> entries =
            new ConcurrentHashMap<>();

    public void record(LedgerEntry entry) {
        if (entry == null || entry.orderId() == null) {
            throw new IllegalArgumentException("A valid ledger entry and order ID are required");
        }
        entries.putIfAbsent(entry.orderId(), entry);
    }

    public Optional<LedgerEntry> findByOrderId(String orderId) {
        return Optional.ofNullable(entries.get(orderId));
    }

public List<LedgerEntry> findAll() {
    return List.copyOf(entries.values());
}

}
