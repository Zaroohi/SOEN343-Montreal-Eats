package com.bakedtogo.presentation;

import com.bakedtogo.business.domain.OrderEvent;
import com.bakedtogo.business.service.OrderItemRequest;
import com.bakedtogo.business.service.OrderService;
import com.bakedtogo.business.view.OrderViewFactory;
import com.bakedtogo.presentation.dto.OrderResponse;
import com.bakedtogo.presentation.dto.PlaceOrderRequest;
import com.bakedtogo.presentation.dto.RejectRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bakedtogo.business.domain.enums.OrderStatus;
import com.bakedtogo.business.domain.enums.Role;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final OrderViewFactory views;

    public OrderController(OrderService service, OrderViewFactory views) {
        this.service = service;
        this.views = views;
    }

    @PostMapping
    public OrderResponse place(@RequestBody PlaceOrderRequest body, HttpServletRequest req) {
        var items = body.lines().stream()
                .map(l -> new OrderItemRequest(l.menuItemId(), l.quantity())).toList();
        return OrderResponse.from(service.placeOrder(ActorResolver.from(req), body.bakeryId(), items));
    }

    @PostMapping("/{id}/accept")
    public OrderResponse accept(@PathVariable String id, HttpServletRequest req) {
        return OrderResponse.from(service.acceptOrder(ActorResolver.from(req), id));
    }

    @PostMapping("/{id}/reject")
    public OrderResponse reject(@PathVariable String id, @RequestBody RejectRequest body,
                                HttpServletRequest req) {
        return OrderResponse.from(service.rejectOrder(ActorResolver.from(req), id, body.reason()));
    }

    @PostMapping("/{id}/prepare")
    public OrderResponse prepare(@PathVariable String id, HttpServletRequest req) {
        return OrderResponse.from(service.startPreparing(ActorResolver.from(req), id));
    }

    @PostMapping("/{id}/ready")
    public OrderResponse ready(@PathVariable String id, HttpServletRequest req) {
        return OrderResponse.from(service.markReady(ActorResolver.from(req), id));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable String id, HttpServletRequest req) {
        return OrderResponse.from(service.cancelOrder(ActorResolver.from(req), id));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable String id, HttpServletRequest req) {
        var order = service.getOrder(ActorResolver.from(req), id);
        return OrderResponse.from(order, null);
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<String> summary(@PathVariable String id,
                                          @RequestParam(defaultValue = "json") String format,
                                          HttpServletRequest req) {
        var actor = ActorResolver.from(req);
        var order = service.getOrder(actor, id);   // permission check happens here
        String body = views.create(actor.role(), format).display(order);
        MediaType type = "text".equalsIgnoreCase(format)
                ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_JSON;
        return ResponseEntity.ok().contentType(type).body(body);
    }

@GetMapping("/{id}/final-summary")
public ResponseEntity<String> finalSummary(
        @PathVariable String id,
        @RequestParam(defaultValue = "json") String format,
        HttpServletRequest req) {

    var actor = ActorResolver.from(req);

    if (actor.role() != Role.CUSTOMER) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only customers can view final order summaries");
    }

    var order = service.getOrder(actor, id);

    if (!order.getCustomerId().equals(actor.id())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only view your own order summary");
    }

    if (order.getStatus() != OrderStatus.DELIVERED) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Order summary is available only after delivery");
    }

    String body = views.create(actor.role(), format).display(order);

    MediaType type = "text".equalsIgnoreCase(format)
            ? MediaType.TEXT_PLAIN
            : MediaType.APPLICATION_JSON;

    return ResponseEntity.ok()
            .contentType(type)
            .body(body);
}

    @GetMapping
    public List<OrderResponse> list(HttpServletRequest req) {
        return service.getOrders(ActorResolver.from(req)).stream()
                .map(o -> OrderResponse.from(o, null)).toList();
    }

    @GetMapping("/{id}/events")
    public List<OrderEvent> events(@PathVariable String id) {
        return service.getEvents(id);
    }
}