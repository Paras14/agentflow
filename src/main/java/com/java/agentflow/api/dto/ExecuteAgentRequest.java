package com.java.agentflow.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Request to execute an agent directly.
 */
public record ExecuteAgentRequest(
        @NotBlank(message = "Agent type is required") String type,

        Map<String, Object> config,

        Map<String, Object> inputs) {
    public ExecuteAgentRequest {
        config = config != null ? config : Map.of();
        inputs = inputs != null ? inputs : Map.of();
    }
}
