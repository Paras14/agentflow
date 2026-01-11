package com.java.agentflow.api.dto;

import com.java.agentflow.workflow.entity.Workflow;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for workflow response.
 */
public record WorkflowResponse(
        UUID id,
        String name,
        String version,
        String description,
        Instant createdAt,
        Instant updatedAt,
        String createdBy) {
    public static WorkflowResponse from(Workflow workflow) {
        return new WorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getVersion(),
                workflow.getDescription(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt(),
                workflow.getCreatedBy());
    }
}
