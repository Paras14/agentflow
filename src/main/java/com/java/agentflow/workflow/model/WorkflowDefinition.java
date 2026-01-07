package com.java.agentflow.workflow.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a parsed workflow definition.
 */
public record WorkflowDefinition(
        String name,
        String version,
        String description,
        List<StepDefinition> steps) {

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Workflow name is required");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow must have at least one step");
        }

        steps.forEach(StepDefinition::validate);

        // check for duplicates
        long uniqueIds = steps.stream().map(StepDefinition::id).distinct().count();
        if (uniqueIds != steps.size()) {
            throw new IllegalArgumentException("Duplicate step IDs found");
        }

        // validate dependencies
        List<String> stepIds = steps.stream().map(StepDefinition::id).toList();
        for (StepDefinition step : steps) {
            if (step.dependsOn() != null) {
                for (String dep : step.dependsOn()) {
                    if (!stepIds.contains(dep)) {
                        throw new IllegalArgumentException("Step '" + step.id() + "' depends on unknown step: " + dep);
                    }
                }
            }
        }
    }

    public record StepDefinition(
            String id,
            String agent,
            Map<String, Object> config,
            List<String> dependsOn,
            RetryConfig retry) {
        public void validate() {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Step ID is required");
            }

            if (!id.matches("^[a-zA-Z][a-zA-Z0-9_-]*$")) {
                throw new IllegalArgumentException("Invalid step ID format: " + id);
            }
            if (agent == null || agent.isBlank()) {
                throw new IllegalArgumentException("Agent type is required for step: " + id);
            }
        }
    }

    public record RetryConfig(
            int maxRetries,
            long delayMs) {
        public RetryConfig {
            if (maxRetries < 0)
                maxRetries = 0;
            if (maxRetries > 10)
                maxRetries = 10;

            if (delayMs < 0)
                delayMs = 0;
            if (delayMs > 60000)
                delayMs = 60000;
        }

        public static RetryConfig defaultConfig() {
            return new RetryConfig(3, 1000);
        }
    }
}
