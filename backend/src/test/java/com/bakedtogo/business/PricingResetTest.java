package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.billing.FareLedger;
import com.bakedtogo.business.billing.LedgerEntry;
import com.bakedtogo.business.billing.LedgerService;
import com.bakedtogo.business.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PricingResetTest {

    @Test
    void resetClearsTheLedgerSoTheSameOrderIdCanBeChargedAgain() {
        FareLedger ledger = new FareLedger();
        LedgerService service = new LedgerService();
        ledger.record(new LedgerEntry("ORD-0001", "c1", "b1", "d1",
                OrderStatus.DELIVERED, Instant.now(), Instant.now(), 10.0));
        service.recordCharge("ORD-0001", BigDecimal.TEN);

        ledger.reset();
        service.reset();

        assertTrue(ledger.findAll().isEmpty());
        assertTrue(service.findAll().isEmpty());
        assertDoesNotThrow(() -> service.recordCharge("ORD-0001", BigDecimal.TEN));
    }
}