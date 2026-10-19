// test/.../business/OrderViewTest.java
package com.bakedtogo.business;

import static org.junit.jupiter.api.Assertions.*;

import com.bakedtogo.business.domain.Order;
import com.bakedtogo.business.domain.OrderLine;
import com.bakedtogo.business.view.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderViewTest {
    private Order order() {
        Order o = new Order("ORD-1", "c1", "b1", List.of(new OrderLine("m1", "Croissant", 3.5, 2)));
        o.setTotal(7.0);
        return o;
    }

    @Test void customerSeesPricesAndTotal() {
        String out = new CustomerOrderView(new ConsoleOrderRenderer()).display(order());
        assertTrue(out.contains("total: 7.0"));
        assertTrue(out.contains("3.50"));
    }

    @Test void bakeryDoesNotSeePrices() {
        String out = new BakeryOrderView(new ConsoleOrderRenderer()).display(order());
        assertFalse(out.contains("total"));
        assertTrue(out.contains("2 x Croissant"));
    }

    @Test void sameViewWorksWithEitherRenderer() {
        String json = new CustomerOrderView(new JsonOrderRenderer()).display(order());
        assertTrue(json.startsWith("{") && json.endsWith("}"));
    }
}