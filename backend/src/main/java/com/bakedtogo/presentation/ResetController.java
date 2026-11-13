package com.bakedtogo.presentation;

import com.bakedtogo.business.service.ResetService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResetController {

    private final ResetService service;

    public ResetController(ResetService service) { this.service = service; }

    @PostMapping("/api/reset")
    public Map<String, String> reset(HttpServletRequest req) {
        service.reset(ActorResolver.from(req));
        return Map.of("status", "reset", "message", "System returned to its initial state");
    }
}