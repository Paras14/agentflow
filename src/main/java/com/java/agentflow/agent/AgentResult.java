package com.java.agentflow.agent;

import java.time.Duration;
import java.util.Map;

/**
 * Result of an agent execution.
 * Contains outputs, success status, and execution metadata.
 */
public record AgentResult(
        /**
         * Whether the execution was successful.
         */
        boolean success,

        /**
         * Output data from the agent execution.
         * Structure depends on the agent type.
         */
        Map<String, Object> outputs,

        /**
         * Error message if execution failed, null otherwise.
         */
        String error,

        /**
         * Time taken to execute the agent.
         */
        Duration executionTime) {
    /**
     * Create a successful result.
     */
    public static AgentResult success(Map<String, Object> outputs, Duration executionTime) {
        return new AgentResult(true, outputs, null, executionTime);
    }

    /**
     * Create a failed result.
     */
    public static AgentResult failure(String error, Duration executionTime) {
        return new AgentResult(false, Map.of(), error, executionTime);
    }

    /**
     * Create a failed result with partial outputs.
     */
    public static AgentResult failure(String error, Map<String, Object> partialOutputs, Duration executionTime) {
        return new AgentResult(false, partialOutputs, error, executionTime);
    }
}
