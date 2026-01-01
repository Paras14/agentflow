package com.java.agentflow.api.dto;

import com.java.agentflow.agent.AgentResult;

import java.util.Map;

/**
 * Response from executing an agent.
 */
public record ExecuteAgentResponse(
        boolean success,
        Map<String, Object> outputs,
        String error,
        long executionTimeMs) {
    public static ExecuteAgentResponse from(AgentResult result) {
        return new ExecuteAgentResponse(
                result.success(),
                result.outputs(),
                result.error(),
                result.executionTime().toMillis());
    }
}
