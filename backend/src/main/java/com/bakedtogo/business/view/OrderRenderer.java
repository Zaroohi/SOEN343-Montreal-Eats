package com.bakedtogo.business.view;

import java.util.Map;

/** Implementor side of the Bridge: formats fields, knows nothing about orders. */
public interface OrderRenderer {
    String render(String title, Map<String, Object> fields);
}