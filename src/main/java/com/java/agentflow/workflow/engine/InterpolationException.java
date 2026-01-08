package com.java.agentflow.workflow.engine;

/**
 * Exception thrown when variable interpolation fails.
 */
public class InterpolationException extends RuntimeException {

    public InterpolationException(String message) {
        super(message);
    }

    public InterpolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
