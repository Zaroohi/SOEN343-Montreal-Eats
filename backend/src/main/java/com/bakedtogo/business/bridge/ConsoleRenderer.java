
package com.bakedtogo.business.bridge;

public class ConsoleRenderer implements OutputRenderer {

    @Override
    public void render(String content) {
        System.out.println("[CONSOLE] " + content);
    }
}
