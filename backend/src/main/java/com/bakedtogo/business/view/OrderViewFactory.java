package com.bakedtogo.business.view;

import com.bakedtogo.business.domain.enums.Role;
import org.springframework.stereotype.Component;

/** Picks the abstraction by role and the implementor by format. */
@Component
public class OrderViewFactory {
    public OrderView create(Role role, String format) {
        OrderRenderer renderer = "text".equalsIgnoreCase(format)
                ? new ConsoleOrderRenderer() : new JsonOrderRenderer();
        return role == Role.CUSTOMER ? new CustomerOrderView(renderer)
                                     : new BakeryOrderView(renderer);
    }
}