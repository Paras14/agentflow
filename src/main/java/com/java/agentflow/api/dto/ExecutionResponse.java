package com.java.agentflow.api.dto;

import com.java.agentflow.workflow.entity.StepExecution;
import com.java.agentflow.workflow.entity.WorkflowExecution;
import com.java.agentflow.workflow.model.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for workflow execution response.
 */
public record ExecutionResponse(
        UUID id,
        UUID workflowId,
        String workflowName,
        ExecutionStatus status,
        Map<String, Object> inputs,
        Map<String, Object> outputs,
        String currentStep,
        String error,
        Instant startedAt,
        Instant completedAt,
        List<StepExecutionResponse> steps) {
    public static ExecutionResponse from(WorkflowExecution execution) {
        return new ExecutionResponse(
                execution.getId(),
                execution.getWorkflow().getId(),
                execution.getWorkflow().getName(),
                execution.getStatus(),
                execution.getInputs(),
                execution.getOutputs(),
                execution.getCurrentStep(),
                execution.getError(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getStepExecutions().stream()
                        .map(StepExecutionResponse::from)
                        .toList());
    }

    /**
     * DTO for step execution within a workflow.
     */
    public record StepExecutionResponse(
            UUID id,
            String stepId,
            String agentType,
            ExecutionStatus status,
            Map<String, Object> outputs,
            String error,
            Instant startedAt,
            Instant completedAt,
            int retryCount) {
        public static StepExecutionResponse from(StepExecution step) {
            return new StepExecutionResponse(
                    step.getId(),
                    step.getStepId(),
                    step.getAgentType(),
                    step.getStatus(),
                    step.getOutputs(),
                    step.getError(),
                    step.getStartedAt(),
                    step.getCompletedAt(),
                    step.getRetryCount());
        }
    }
}
