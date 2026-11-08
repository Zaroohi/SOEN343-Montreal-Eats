package com.bakedtogo.presentation;

import com.bakedtogo.business.domain.BakeryStaff;
import com.bakedtogo.business.domain.User;
import com.bakedtogo.business.domain.exception.UnauthenticatedException;
import com.bakedtogo.business.service.Actor;
import jakarta.servlet.http.HttpServletRequest;

/** Translates REG's authenticated user (set by SessionInterceptor) into ORD's Actor. */
public final class ActorResolver {
    private ActorResolver() {}

    public static Actor from(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        if (user == null) {
            throw new UnauthenticatedException("Login required");
        }
        String bakeryId = (user instanceof BakeryStaff s) ? s.getBakeryId() : null;
        return new Actor(user.getId(), user.getRole(), bakeryId);
    }
}