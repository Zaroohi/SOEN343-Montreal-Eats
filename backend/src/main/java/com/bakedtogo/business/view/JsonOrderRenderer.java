package com.bakedtogo.business.view;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonOrderRenderer implements OrderRenderer {
    public String render(String title, Map<String, Object> fields) {
        StringBuilder sb = new StringBuilder("{\"title\":").append(quote(title));
        fields.forEach((k, v) -> sb.append(",").append(quote(k)).append(":").append(value(v)));
        return sb.append("}").toString();
    }

    private String value(Object v) {
        if (v instanceof Number) return v.toString();
        if (v instanceof Collection<?> c) {
            return c.stream().map(x -> quote(String.valueOf(x)))
                    .collect(Collectors.joining(",", "[", "]"));
        }
        return quote(String.valueOf(v));
    }

    private String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}