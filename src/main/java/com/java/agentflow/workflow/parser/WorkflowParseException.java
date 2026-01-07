package com.java.agentflow.workflow.parser;

/**
 * Exception thrown when workflow parsing fails.
 */
public class WorkflowParseException extends RuntimeException {

    public WorkflowParseException(String message) {
        super(message);
    }

    public WorkflowParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
