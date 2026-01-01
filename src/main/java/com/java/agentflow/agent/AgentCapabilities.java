package com.java.agentflow.agent;

/**
 * Describes an agent's capabilities and metadata.
 * Used for documentation, validation, and the future agent marketplace.
 */
public record AgentCapabilities(
        /**
         * The agent type identifier.
         */
        String type,

        /**
         * Human-readable description of what this agent does.
         */
        String description) {
}
