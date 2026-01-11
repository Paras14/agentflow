package com.java.agentflow.workflow.repository;

import com.java.agentflow.workflow.entity.WorkflowExecution;
import com.java.agentflow.workflow.model.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for WorkflowExecution entities.
 */
@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

    /**
     * Find executions for a specific workflow.
     */
    Page<WorkflowExecution> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId, Pageable pageable);

    /**
     * Find executions by status.
     */
    List<WorkflowExecution> findByStatus(ExecutionStatus status);

    /**
     * Find running executions (for recovery on restart).
     */
    List<WorkflowExecution> findByStatusIn(List<ExecutionStatus> statuses);
}
