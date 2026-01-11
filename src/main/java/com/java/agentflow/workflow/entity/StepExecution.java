package com.java.agentflow.workflow.entity;

import com.java.agentflow.workflow.model.ExecutionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity tracking individual step execution within a workflow.
 */
@Entity
@Table(name = "step_executions")
public class StepExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private WorkflowExecution execution;

    @Column(name = "step_id", nullable = false)
    private String stepId;

    @Column(name = "agent_type", nullable = false)
    private String agentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status = ExecutionStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> inputs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> outputs;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(name = "retry_count")
    private int retryCount = 0;

    // Helper methods
    public void markRunning() {
        this.status = ExecutionStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markCompleted(Map<String, Object> outputs) {
        this.status = ExecutionStatus.COMPLETED;
        this.outputs = outputs;
        this.completedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.status = ExecutionStatus.FAILED;
        this.error = error;
        this.completedAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
        this.status = ExecutionStatus.PENDING;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public WorkflowExecution getExecution() {
        return execution;
    }

    public void setExecution(WorkflowExecution execution) {
        this.execution = execution;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
