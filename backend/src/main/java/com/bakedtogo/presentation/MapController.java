package com.bakedtogo.presentation;

import com.bakedtogo.business.service.MapService;
import com.bakedtogo.business.service.MapView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MapController {
    private final MapService service;

    public MapController(MapService service) { this.service = service; }

    @GetMapping("/api/map")
    public MapView map(HttpServletRequest req) {
        return service.view(ActorResolver.from(req));
    }
}