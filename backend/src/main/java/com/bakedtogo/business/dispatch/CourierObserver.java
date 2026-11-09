package com.bakedtogo.business.dispatch;

import com.bakedtogo.business.domain.CourierProfile;

/** Observer interface: told when a courier becomes available for work. */
public interface CourierObserver {
    void update(CourierProfile courier);
}