package com.java.agentflow.workflow.service;

/**
 * Exception thrown when a workflow already exists.
 */
public class WorkflowAlreadyExistsException extends RuntimeException {
    public WorkflowAlreadyExistsException(String message) {
        super(message);
    }
}
