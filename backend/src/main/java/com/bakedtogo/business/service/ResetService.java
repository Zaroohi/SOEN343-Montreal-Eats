package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.enums.Role;
import com.bakedtogo.business.domain.exception.ForbiddenException;
import com.bakedtogo.business.reset.Resettable;
import com.bakedtogo.infrastructure.CityLoader;
import com.bakedtogo.infrastructure.EventLogger;
import java.util.List;
import org.springframework.stereotype.Service;

/** Returns the system to its initial state: clears all activity, then reloads city.json. */
@Service
public class ResetService {

    private final List<Resettable> parts;
    private final CityLoader city;

    public ResetService(List<Resettable> parts, CityLoader city) {
        this.parts = parts;
        this.city = city;
    }

    public synchronized void reset(Actor actor) {
        if (actor == null || actor.role() != Role.OPERATOR) {
            throw new ForbiddenException("Only operators can reset the system");
        }
        parts.forEach(Resettable::reset);        // orders, events, queue, couriers, bakeries...
        EventLogger.getInstance().reset();       // event ids start again at EVT-0001
        city.load();                             // bakeries, menus, couriers, customers from city.json
        System.out.println("[RESET] System returned to its initial state ("
                + parts.size() + " components cleared, city reloaded)");
    }
}