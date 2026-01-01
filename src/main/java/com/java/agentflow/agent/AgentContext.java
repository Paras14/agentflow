package com.java.agentflow.agent;

import java.util.Map;

/**
 * Context provided to an agent during execution.
 * Contains all inputs and configuration needed for the agent to run.
 */
public record AgentContext(
        /**
         * Input parameters passed to this agent.
         * Can include data from previous steps or workflow inputs.
         */
        Map<String, Object> inputs,

        /**
         * Agent-specific configuration.
         * For HttpAgent: url, method, headers, body, timeout
         * For LlmAgent: model, prompt, temperature
         */
        Map<String, Object> config) {
    public AgentContext {
        // Ensure immutability
        inputs = inputs != null ? Map.copyOf(inputs) : Map.of();
        config = config != null ? Map.copyOf(config) : Map.of();
    }

    /**
     * Get a config value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    /**
     * Get a required config value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getRequiredConfig(String key) {
        Object value = config.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required config: " + key);
        }
        return (T) value;
    }
}
