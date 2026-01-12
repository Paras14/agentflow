package com.java.agentflow.api;

import com.java.agentflow.api.dto.ExecuteWorkflowRequest;
import com.java.agentflow.api.dto.ExecutionResponse;
import com.java.agentflow.api.dto.WorkflowResponse;
import com.java.agentflow.workflow.entity.Workflow;
import com.java.agentflow.workflow.entity.WorkflowExecution;
import com.java.agentflow.workflow.service.WorkflowNotFoundException;
import com.java.agentflow.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API for workflow CRUD and execution.
 */
@RestController
@RequestMapping("/api/workflows")
@Tag(name = "Workflows", description = "Create, manage, and execute workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping(consumes = { MediaType.TEXT_PLAIN_VALUE, "application/x-yaml", "text/yaml" })
    @Operation(summary = "Create a workflow from YAML definition")
    public ResponseEntity<WorkflowResponse> createWorkflow(
            @RequestBody String yaml,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Workflow workflow = workflowService.createFromYaml(yaml, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkflowResponse.from(workflow));
    }

    @GetMapping
    @Operation(summary = "List all workflows")
    public Page<WorkflowResponse> listWorkflows(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return workflowService.findAll(pageable).map(WorkflowResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a workflow by ID")
    public ResponseEntity<WorkflowResponse> getWorkflow(
            @PathVariable UUID id) {
        return workflowService.findById(id)
                .map(WorkflowResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a workflow")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable UUID id) {
        workflowService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute a workflow")
    public ResponseEntity<ExecutionResponse> executeWorkflow(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ExecuteWorkflowRequest request) {
        var inputs = request != null ? request.inputs() : java.util.Map.<String, Object>of();
        WorkflowExecution execution = workflowService.execute(id, inputs);
        return ResponseEntity.ok(ExecutionResponse.from(execution));
    }

    @GetMapping("/{id}/executions")
    @Operation(summary = "Get execution history for a workflow")
    public Page<ExecutionResponse> getExecutions(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return workflowService.findExecutionsByWorkflow(id, pageable)
                .map(ExecutionResponse::from);
    }
}
