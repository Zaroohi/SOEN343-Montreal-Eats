package com.bakedtogo.business.view;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleOrderRenderer implements OrderRenderer {
    public String render(String title, Map<String, Object> fields) {
        StringBuilder sb = new StringBuilder("=== ").append(title).append(" ===\n");
        fields.forEach((k, v) -> sb.append(k).append(": ").append(format(v)).append("\n"));
        return sb.toString();
    }

    private String format(Object v) {
        if (v instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).collect(Collectors.joining("; "));
        }
        return String.valueOf(v);
    }
}