package com.java.agentflow.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of all available agents in the system.
 * Automatically discovers agents via Spring's component scanning.
 */
@Component
public class AgentRegistry {

    private final Map<String, Agent> agents;

    public AgentRegistry(List<Agent> agentList) {
        this.agents = agentList.stream()
                .collect(Collectors.toMap(Agent::getType, Function.identity()));
    }

    /**
     * Get an agent by its type.
     */
    public Optional<Agent> getAgent(String type) {
        return Optional.ofNullable(agents.get(type));
    }

    /**
     * Get an agent by type, throwing if not found.
     */
    public Agent getAgentOrThrow(String type) {
        return getAgent(type)
                .orElseThrow(() -> new IllegalArgumentException("Unknown agent type: " + type));
    }

    /**
     * Get all registered agents.
     */
    public Map<String, Agent> getAllAgents() {
        return Map.copyOf(agents);
    }

    /**
     * Get capabilities of all registered agents.
     */
    public List<AgentCapabilities> getAllCapabilities() {
        return agents.values().stream()
                .map(Agent::getCapabilities)
                .toList();
    }
}
