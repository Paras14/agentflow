package com.java.agentflow.workflow.service;

import com.java.agentflow.workflow.entity.Workflow;
import com.java.agentflow.workflow.entity.WorkflowExecution;
import com.java.agentflow.workflow.engine.WorkflowExecutor;
import com.java.agentflow.workflow.model.WorkflowDefinition;
import com.java.agentflow.workflow.parser.WorkflowParser;
import com.java.agentflow.workflow.repository.WorkflowExecutionRepository;
import com.java.agentflow.workflow.repository.WorkflowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for workflow operations.
 * Orchestrates between repositories, parser, and executor.
 */
@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowParser workflowParser;
    private final WorkflowExecutor workflowExecutor;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            WorkflowExecutionRepository executionRepository,
            WorkflowParser workflowParser,
            WorkflowExecutor workflowExecutor) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.workflowParser = workflowParser;
        this.workflowExecutor = workflowExecutor;
    }

    /**
     * Create a new workflow from YAML definition.
     */
    @Transactional
    public Workflow createFromYaml(String yaml, String createdBy) {
        // Parse and validate
        WorkflowDefinition definition = workflowParser.parseYaml(yaml);

        // Check for duplicate
        if (workflowRepository.existsByNameAndVersion(definition.name(), definition.version())) {
            throw new WorkflowAlreadyExistsException(
                    "Workflow already exists: " + definition.name() + " v" + definition.version());
        }

        // Create entity
        Workflow workflow = new Workflow();
        workflow.setName(definition.name());
        workflow.setVersion(definition.version());
        workflow.setDescription(definition.description());
        workflow.setDefinition(workflowParser.toMap(definition));
        workflow.setCreatedBy(createdBy);

        return workflowRepository.save(workflow);
    }

    /**
     * Get a workflow by ID.
     */
    public Optional<Workflow> findById(UUID id) {
        return workflowRepository.findById(id);
    }

    /**
     * Get all workflows with pagination.
     */
    public Page<Workflow> findAll(Pageable pageable) {
        return workflowRepository.findAll(pageable);
    }

    /**
     * Execute a workflow synchronously.
     */
    @Transactional
    public WorkflowExecution execute(UUID workflowId, Map<String, Object> inputs) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found: " + workflowId));

        // Create execution record
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflow(workflow);
        execution.setInputs(inputs);
        execution = executionRepository.save(execution);

        // Execute workflow
        return workflowExecutor.execute(workflow, execution, inputs);
    }

    /**
     * Get execution by ID.
     */
    public Optional<WorkflowExecution> findExecutionById(UUID id) {
        return executionRepository.findById(id);
    }

    /**
     * Get executions for a workflow.
     */
    public Page<WorkflowExecution> findExecutionsByWorkflow(UUID workflowId, Pageable pageable) {
        return executionRepository.findByWorkflowIdOrderByCreatedAtDesc(workflowId, pageable);
    }

    /**
     * Delete a workflow and all its executions.
     */
    @Transactional
    public void delete(UUID id) {
        if (!workflowRepository.existsById(id)) {
            throw new WorkflowNotFoundException("Workflow not found: " + id);
        }
        workflowRepository.deleteById(id);
    }
}
