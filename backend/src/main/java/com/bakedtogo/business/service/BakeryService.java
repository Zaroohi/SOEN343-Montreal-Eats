package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.enums.ItemStatus;
import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.domain.exception.BusinessRuleException;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.domain.exception.NotFoundException;
import com.bakedtogo.persistence.BakeryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BakeryService {

    private final BakeryRepository bakeries;

    public BakeryService(BakeryRepository bakeries) {
        this.bakeries = bakeries;
    }

    public List<Bakery> listBakeries(Actor actor) {
        requireLoggedIn(actor);
        return bakeries.findAll();
    }

    /** UC-ORD-02: any logged-in role may browse the menu of an open bakery. */
    public List<MenuItem> getMenu(Actor actor, String bakeryId) {
        requireLoggedIn(actor);
        Bakery bakery = find(bakeryId);
        if (bakery.getStatus() != BakeryStatus.OPEN) {
            throw new BusinessRuleException("Bakery " + bakery.getName() + " is not open");
        }
        return bakery.getMenu();
    }

    /** UC-ORD-08: staff pause their own bakery, operators any bakery. */
    public synchronized Bakery pause(Actor actor, String bakeryId) {
        Bakery bakery = find(bakeryId);
        requireStaffOfOrOperator(actor, bakeryId);
        bakery.setStatus(BakeryStatus.PAUSED);   // in-flight orders keep going
        return bakery;
    }

    /** Only operators may reopen. */
    public synchronized Bakery reopen(Actor actor, String bakeryId) {
        requireLoggedIn(actor);
        if (actor.role() != Role.OPERATOR) {
            throw new ForbiddenException("Only operators can reopen a bakery");
        }
        Bakery bakery = find(bakeryId);
        bakery.setStatus(BakeryStatus.OPEN);
        return bakery;
    }

    /** UC-ORD-06: change an item's availability. */
    public synchronized MenuItem setItemStatus(Actor actor, String bakeryId,
                                               String itemId, ItemStatus status) {
        requireOwnStaff(actor, bakeryId);
        MenuItem item = findItem(find(bakeryId), itemId);
        item.setStatus(status);
        return item;
    }

    /** UC-ORD-06: edit name and/or price. */
    public synchronized MenuItem updateItem(Actor actor, String bakeryId, String itemId,
                                            String name, Double price) {
        requireOwnStaff(actor, bakeryId);
        MenuItem item = findItem(find(bakeryId), itemId);
        if (price != null) {
            if (price < 0) throw new BusinessRuleException("Price cannot be negative");
            item.setPrice(price);
        }
        if (name != null && !name.isBlank()) item.setName(name);
        return item;
    }

    // ---------- helpers ----------

    private void requireLoggedIn(Actor actor) {
        if (actor == null) throw new ForbiddenException("No authenticated user");
    }

    private void requireOwnStaff(Actor actor, String bakeryId) {
        requireLoggedIn(actor);
        if (actor.role() != Role.BAKERY_STAFF || !bakeryId.equals(actor.bakeryId())) {
            throw new ForbiddenException("Only staff of this bakery may do this");
        }
    }

    private void requireStaffOfOrOperator(Actor actor, String bakeryId) {
        requireLoggedIn(actor);
        boolean ok = actor.role() == Role.OPERATOR
                || (actor.role() == Role.BAKERY_STAFF && bakeryId.equals(actor.bakeryId()));
        if (!ok) throw new ForbiddenException("You may not manage this bakery");
    }

    private Bakery find(String id) {
        return bakeries.findById(id)
                .orElseThrow(() -> new NotFoundException("Bakery " + id + " not found"));
    }

    private MenuItem findItem(Bakery bakery, String itemId) {
        return bakery.getMenu().stream().filter(m -> m.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Menu item " + itemId + " not found"));
    }
}