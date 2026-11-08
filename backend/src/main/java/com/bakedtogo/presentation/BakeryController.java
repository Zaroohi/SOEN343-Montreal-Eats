package com.bakedtogo.presentation;

import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.service.BakeryService;
import com.bakedtogo.presentation.dto.BakeryResponse;
import com.bakedtogo.presentation.dto.ItemUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bakeries")
public class BakeryController {

    private final BakeryService service;

    public BakeryController(BakeryService service) { this.service = service; }

    @GetMapping
    public List<BakeryResponse> list(HttpServletRequest req) {
        return service.listBakeries(ActorResolver.from(req)).stream()
                .map(BakeryResponse::from).toList();
    }

    @GetMapping("/{id}/menu")
    public List<MenuItem> menu(@PathVariable String id, HttpServletRequest req) {
        return service.getMenu(ActorResolver.from(req), id);
    }

    @PostMapping("/{id}/pause")
    public BakeryResponse pause(@PathVariable String id, HttpServletRequest req) {
        return BakeryResponse.from(service.pause(ActorResolver.from(req), id));
    }

    @PostMapping("/{id}/reopen")
    public BakeryResponse reopen(@PathVariable String id, HttpServletRequest req) {
        return BakeryResponse.from(service.reopen(ActorResolver.from(req), id));
    }

    @PutMapping("/{id}/menu/{itemId}")
    public MenuItem updateItem(@PathVariable String id, @PathVariable String itemId,
                               @RequestBody ItemUpdateRequest body, HttpServletRequest req) {
        var actor = ActorResolver.from(req);
        if (body.name() != null || body.price() != null) {
            service.updateItem(actor, id, itemId, body.name(), body.price());
        }
        if (body.status() != null) {
            return service.setItemStatus(actor, id, itemId, body.status());
        }
        return service.updateItem(actor, id, itemId, null, null);
    }
}