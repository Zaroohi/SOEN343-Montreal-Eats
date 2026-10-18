package com.bakedtogo.business.domain.exception;

// Thrown when a requested status change would skip or violate the lifecycle
public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(String message) { super(message); }
}