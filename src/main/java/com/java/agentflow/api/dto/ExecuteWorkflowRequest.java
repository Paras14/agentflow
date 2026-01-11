package com.java.agentflow.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request to execute a workflow.
 */
public record ExecuteWorkflowRequest(
        @NotNull Map<String, Object> inputs) {
    public ExecuteWorkflowRequest {
        inputs = inputs != null ? inputs : Map.of();
    }
}
