package com.java.agentflow.workflow.service;

import com.java.agentflow.async.WorkflowProducer;
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

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowParser workflowParser;
    private final WorkflowExecutor workflowExecutor;
    private final WorkflowProducer workflowProducer;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            WorkflowExecutionRepository executionRepository,
            WorkflowParser workflowParser,
            WorkflowExecutor workflowExecutor,
            WorkflowProducer workflowProducer) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.workflowParser = workflowParser;
        this.workflowExecutor = workflowExecutor;
        this.workflowProducer = workflowProducer;
    }

    @Transactional
    public Workflow createFromYaml(String yaml, String createdBy) {
        WorkflowDefinition definition = workflowParser.parseYaml(yaml);

        if (workflowRepository.existsByNameAndVersion(definition.name(), definition.version())) {
            throw new WorkflowAlreadyExistsException(
                    "Workflow already exists: " + definition.name() + " v" + definition.version());
        }

        Workflow workflow = new Workflow();
        workflow.setName(definition.name());
        workflow.setVersion(definition.version());
        workflow.setDescription(definition.description());
        workflow.setDefinition(workflowParser.toMap(definition));
        workflow.setCreatedBy(createdBy);

        return workflowRepository.save(workflow);
    }

    public Optional<Workflow> findById(UUID id) {
        return workflowRepository.findById(id);
    }

    public Page<Workflow> findAll(Pageable pageable) {
        return workflowRepository.findAll(pageable);
    }

    @Transactional
    public WorkflowExecution execute(UUID workflowId, Map<String, Object> inputs) {
        return execute(workflowId, inputs, false);
    }

    @Transactional
    public WorkflowExecution execute(UUID workflowId, Map<String, Object> inputs, boolean async) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found: " + workflowId));

        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflow(workflow);
        execution.setInputs(inputs);
        execution = executionRepository.save(execution);

        if (async) {
            workflowProducer.queueExecution(workflowId, execution.getId(), inputs);
            return execution;
        }

        return workflowExecutor.execute(workflow, execution, inputs);
    }

    public Optional<WorkflowExecution> findExecutionById(UUID id) {
        return executionRepository.findById(id);
    }

    public Page<WorkflowExecution> findExecutionsByWorkflow(UUID workflowId, Pageable pageable) {
        return executionRepository.findByWorkflowIdOrderByCreatedAtDesc(workflowId, pageable);
    }

    @Transactional
    public void delete(UUID id) {
        if (!workflowRepository.existsById(id)) {
            throw new WorkflowNotFoundException("Workflow not found: " + id);
        }
        workflowRepository.deleteById(id);
    }
}
