package com.java.agentflow.async;

import com.java.agentflow.workflow.entity.Workflow;
import com.java.agentflow.workflow.entity.WorkflowExecution;
import com.java.agentflow.workflow.engine.WorkflowExecutor;
import com.java.agentflow.workflow.repository.WorkflowExecutionRepository;
import com.java.agentflow.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AsyncExecutionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowExecutor workflowExecutor;

    public AsyncExecutionService(
            WorkflowRepository workflowRepository,
            WorkflowExecutionRepository executionRepository,
            WorkflowExecutor workflowExecutor) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.workflowExecutor = workflowExecutor;
    }

    @Transactional
    public void executeWorkflow(UUID workflowId, UUID executionId, java.util.Map<String, Object> inputs) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

        workflowExecutor.execute(workflow, execution, inputs);
    }

    @Transactional
    public void markExecutionFailed(UUID executionId, String error) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            execution.markFailed(error);
            executionRepository.save(execution);
        });
    }
}
