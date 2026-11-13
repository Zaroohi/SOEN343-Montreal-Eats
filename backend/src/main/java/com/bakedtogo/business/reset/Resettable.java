
package com.bakedtogo.business.reset;

/** Anything that holds in-memory state and can return to its starting point. */
public interface Resettable {
    void reset();
}