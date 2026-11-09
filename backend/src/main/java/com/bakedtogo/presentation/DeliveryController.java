package com.bakedtogo.presentation;

import com.bakedtogo.business.service.CourierView;
import com.bakedtogo.business.service.DeliveryService;
import com.bakedtogo.presentation.dto.DutyRequest;
import com.bakedtogo.presentation.dto.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) { this.service = service; }

    @GetMapping("/me")
    public CourierView me(HttpServletRequest req) {
        return service.myView(ActorResolver.from(req));
    }

    @PostMapping("/duty")
    public CourierView duty(@RequestBody DutyRequest body, HttpServletRequest req) {
        return service.toggleDuty(ActorResolver.from(req), body.onDuty());
    }

    @PostMapping("/accept")
    public CourierView accept(HttpServletRequest req) {
        return service.accept(ActorResolver.from(req));
    }

    @PostMapping("/decline")
    public CourierView decline(HttpServletRequest req) {
        return service.decline(ActorResolver.from(req));
    }

    @PostMapping("/pickup")
    public OrderResponse pickup(HttpServletRequest req) {
        return OrderResponse.from(service.confirmPickup(ActorResolver.from(req)));
    }

    @PostMapping("/deliver")
    public OrderResponse deliver(HttpServletRequest req) {
        return OrderResponse.from(service.confirmDelivery(ActorResolver.from(req)));
    }

    // ---- operator ----

    @GetMapping("/couriers")
    public List<CourierView> couriers(HttpServletRequest req) {
        return service.listCouriers(ActorResolver.from(req));
    }

    @GetMapping("/queue")
    public List<String> queue(HttpServletRequest req) {
        return service.getQueue(ActorResolver.from(req));
    }

    @GetMapping("/stalled")
    public List<OrderResponse> stalled(HttpServletRequest req) {
        return service.getStalled(ActorResolver.from(req)).stream()
                .map(o -> OrderResponse.from(o, null)).toList();
    }

    @PostMapping("/orders/{id}/reassign")
    public OrderResponse reassign(@PathVariable String id, HttpServletRequest req) {
        return OrderResponse.from(service.reassign(ActorResolver.from(req), id), null);
    }
}