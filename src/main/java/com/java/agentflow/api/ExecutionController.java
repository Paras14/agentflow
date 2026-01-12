package com.java.agentflow.api;

import com.java.agentflow.api.dto.ExecutionResponse;
import com.java.agentflow.workflow.service.WorkflowNotFoundException;
import com.java.agentflow.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API for workflow execution operations.
 */
@RestController
@RequestMapping("/api/executions")
@Tag(name = "Executions", description = "Monitor and manage workflow executions")
public class ExecutionController {

    private final WorkflowService workflowService;

    public ExecutionController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get execution details by ID")
    public ResponseEntity<ExecutionResponse> getExecution(@PathVariable UUID id) {
        return workflowService.findExecutionById(id)
                .map(ExecutionResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new WorkflowNotFoundException("Execution not found: " + id));
    }
}
