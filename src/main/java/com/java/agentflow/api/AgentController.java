package com.java.agentflow.api;

import com.java.agentflow.agent.Agent;
import com.java.agentflow.agent.AgentContext;
import com.java.agentflow.agent.AgentRegistry;
import com.java.agentflow.agent.AgentResult;
import com.java.agentflow.api.dto.AgentInfo;
import com.java.agentflow.api.dto.ExecuteAgentRequest;
import com.java.agentflow.api.dto.ExecuteAgentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for agent operations.
 */
@RestController
@RequestMapping("/api/agents")
@Tag(name = "Agents", description = "Execute and manage agents")
public class AgentController {

    private final AgentRegistry agentRegistry;

    public AgentController(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @GetMapping
    @Operation(summary = "List all available agents")
    public List<AgentInfo> listAgents() {
        return agentRegistry.getAllCapabilities().stream()
                .map(AgentInfo::from)
                .toList();
    }

    @PostMapping("/execute")
    @Operation(summary = "Execute an agent with the given configuration")
    public ResponseEntity<ExecuteAgentResponse> executeAgent(
            @Valid @RequestBody ExecuteAgentRequest request) {
        // Get the agent
        Agent agent = agentRegistry.getAgentOrThrow(request.type());

        // Build context
        AgentContext context = new AgentContext(request.inputs(), request.config());

        // Execute
        AgentResult result = agent.execute(context);

        // Return response
        ExecuteAgentResponse response = ExecuteAgentResponse.from(result);

        if (result.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }

    @GetMapping("/{type}")
    @Operation(summary = "Get details about a specific agent type")
    public ResponseEntity<AgentInfo> getAgent(@PathVariable String type) {
        return agentRegistry.getAgent(type)
                .map(agent -> ResponseEntity.ok(AgentInfo.from(agent.getCapabilities())))
                .orElse(ResponseEntity.notFound().build());
    }
}
