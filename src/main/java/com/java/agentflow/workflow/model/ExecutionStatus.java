package com.java.agentflow.workflow.model;

/**
 * Execution status for workflows and steps.
 */
public enum ExecutionStatus {
    PENDING, // Not yet started
    RUNNING, // Currently executing
    COMPLETED, // Finished successfully
    FAILED, // Finished with error
    CANCELLED, // Manually cancelled
    SKIPPED // Skipped due to condition or dependency failure
}
