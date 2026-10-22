package com.bakedtogo.presentation;

import com.bakedtogo.business.domain.User;
import com.bakedtogo.business.service.Actor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
    @GetMapping("/api/me")
    public Map<String, String> me(HttpServletRequest req) {
        Actor a = ActorResolver.from(req);
        User u = (User) req.getAttribute("user");
        return Map.of("id", a.id(), "name", u.getName(),
                "role", a.role().name(),
                "bakeryId", a.bakeryId() == null ? "" : a.bakeryId());
    }
}