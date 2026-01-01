package com.java.agentflow.agent;

/**
 * Core interface for all agents in the system.
 * Each agent type implements this interface to provide specific functionality.
 */
public interface Agent {

    /**
     * Returns the unique type identifier for this agent.
     * Examples: "http", "llm", "transform", "sql"
     */
    String getType();

    /**
     * Executes the agent with the given context.
     * 
     * @param context The execution context containing inputs and configuration
     * @return The result of the execution
     */
    AgentResult execute(AgentContext context);

    /**
     * Returns the capabilities and metadata for this agent.
     * Used for documentation and validation.
     */
    default AgentCapabilities getCapabilities() {
        return new AgentCapabilities(getType(), "No description provided");
    }
}
