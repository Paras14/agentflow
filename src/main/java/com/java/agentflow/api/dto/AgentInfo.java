package com.java.agentflow.api.dto;

import com.java.agentflow.agent.AgentCapabilities;

/**
 * DTO for agent metadata.
 */
public record AgentInfo(
        String type,
        String description) {
    public static AgentInfo from(AgentCapabilities capabilities) {
        return new AgentInfo(capabilities.type(), capabilities.description());
    }
}
