package com.bakedtogo.business.bridge;

public class WebRenderer implements OutputRenderer {

    @Override
    public void render(String content) {
        System.out.println("[WEB] " + content);
    }
}

