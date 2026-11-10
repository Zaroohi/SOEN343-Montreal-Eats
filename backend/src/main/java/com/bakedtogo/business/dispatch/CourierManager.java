package com.bakedtogo.business.dispatch;

import com.bakedtogo.business.domain.CourierProfile;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** Subject of the Observer pattern (R-DLV-04). */
@Component
public class CourierManager {
    private final List<CourierObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(CourierObserver observer) { observers.add(observer); }

    public void notifyObservers(CourierProfile courier) {
        for (CourierObserver o : observers) o.update(courier);
    }

    /** Call after any change that may have freed a courier (on duty, delivery done, offer declined). */
    public void updateAvailability(CourierProfile courier) {
        if (courier.isAvailable()) notifyObservers(courier);
    }
}