package com.java.agentflow.workflow.engine;

import com.java.agentflow.agent.Agent;
import com.java.agentflow.agent.AgentContext;
import com.java.agentflow.agent.AgentRegistry;
import com.java.agentflow.agent.AgentResult;
import com.java.agentflow.workflow.entity.StepExecution;
import com.java.agentflow.workflow.entity.Workflow;
import com.java.agentflow.workflow.entity.WorkflowExecution;
import com.java.agentflow.workflow.model.ExecutionStatus;
import com.java.agentflow.workflow.model.WorkflowDefinition;
import com.java.agentflow.workflow.model.WorkflowDefinition.StepDefinition;
import com.java.agentflow.workflow.parser.WorkflowParser;
import com.java.agentflow.workflow.repository.WorkflowExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the execution of workflows.
 */
@Component
public class WorkflowExecutor {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);

    private final AgentRegistry agentRegistry;
    private final WorkflowParser workflowParser;
    private final VariableInterpolator interpolator;
    private final DependencyResolver dependencyResolver;
    private final WorkflowExecutionRepository executionRepository;

    public WorkflowExecutor(
            AgentRegistry agentRegistry,
            WorkflowParser workflowParser,
            VariableInterpolator interpolator,
            WorkflowExecutionRepository executionRepository) {
        this.agentRegistry = agentRegistry;
        this.workflowParser = workflowParser;
        this.interpolator = interpolator;
        this.dependencyResolver = new DependencyResolver();
        this.executionRepository = executionRepository;
    }

    @Transactional
    public WorkflowExecution execute(Workflow workflow, WorkflowExecution execution, Map<String, Object> inputs) {
        log.info("Starting workflow: {} ({})", workflow.getName(), execution.getId());

        try {
            WorkflowDefinition definition = workflowParser.parseFromMap(workflow.getDefinition());
            List<StepDefinition> orderedSteps = dependencyResolver.resolve(definition.steps());

            execution.markRunning();
            execution.setInputs(inputs);
            executionRepository.save(execution);

            Map<String, Map<String, Object>> stepOutputs = new HashMap<>();

            for (StepDefinition stepDef : orderedSteps) {
                execution.setCurrentStep(stepDef.id());
                executionRepository.save(execution);

                StepExecution stepExecution = executeStep(stepDef, inputs, stepOutputs, execution);

                if (stepExecution.getStatus() == ExecutionStatus.FAILED) {
                    execution.markFailed("Step failed: " + stepDef.id() + " - " + stepExecution.getError());
                    return executionRepository.save(execution);
                }

                // Wrap outputs in 'outputs' key so ${steps['stepId'].outputs.xxx} works
                Map<String, Object> stepData = new HashMap<>();
                stepData.put("outputs", stepExecution.getOutputs());
                stepOutputs.put(stepDef.id(), stepData);
            }

            Map<String, Object> workflowOutputs = new HashMap<>();
            workflowOutputs.put("steps", stepOutputs);

            execution.markCompleted(workflowOutputs);
            log.info("Workflow completed: {}", execution.getId());

            return executionRepository.save(execution);

        } catch (Exception e) {
            log.error("Workflow execution failed: {}", execution.getId(), e);
            execution.markFailed(e.getMessage());
            return executionRepository.save(execution);
        }
    }

    private StepExecution executeStep(
            StepDefinition stepDef,
            Map<String, Object> workflowInputs,
            Map<String, Map<String, Object>> stepOutputs,
            WorkflowExecution workflowExecution) {
        log.debug("Executing step: {} (agent: {})", stepDef.id(), stepDef.agent());

        StepExecution stepExecution = new StepExecution();
        stepExecution.setStepId(stepDef.id());
        stepExecution.setAgentType(stepDef.agent());
        workflowExecution.addStepExecution(stepExecution);

        try {
            Agent agent = agentRegistry.getAgentOrThrow(stepDef.agent());

            VariableInterpolator.InterpolationContext interpContext = new VariableInterpolator.InterpolationContext(
                    workflowInputs, stepOutputs);

            @SuppressWarnings("unchecked")
            Map<String, Object> interpolatedConfig = (Map<String, Object>) interpolator
                    .interpolateObject(stepDef.config(), interpContext);
            stepExecution.setInputs(interpolatedConfig);
            stepExecution.markRunning();

            AgentContext agentContext = new AgentContext(Map.of(), interpolatedConfig);
            AgentResult result = agent.execute(agentContext);

            if (result.success()) {
                stepExecution.markCompleted(result.outputs());
                log.debug("Step completed: {} in {}ms", stepDef.id(), result.executionTime().toMillis());
            } else {
                if (shouldRetry(stepDef, stepExecution)) {
                    return retryStep(stepDef, workflowInputs, stepOutputs, workflowExecution, stepExecution);
                }
                stepExecution.markFailed(result.error());
                log.warn("Step failed: {} - {}", stepDef.id(), result.error());
            }

        } catch (Exception e) {
            log.error("Step execution error: {}", stepDef.id(), e);
            stepExecution.markFailed(e.getMessage());
        }

        return stepExecution;
    }

    private boolean shouldRetry(StepDefinition stepDef, StepExecution stepExecution) {
        if (stepDef.retry() == null) {
            return false;
        }
        return stepExecution.getRetryCount() < stepDef.retry().maxRetries();
    }

    private StepExecution retryStep(
            StepDefinition stepDef,
            Map<String, Object> workflowInputs,
            Map<String, Map<String, Object>> stepOutputs,
            WorkflowExecution workflowExecution,
            StepExecution previousAttempt) {
        log.info("Retrying step: {} (attempt {})", stepDef.id(), previousAttempt.getRetryCount() + 1);

        if (stepDef.retry() != null && stepDef.retry().delayMs() > 0) {
            try {
                Thread.sleep(stepDef.retry().delayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        previousAttempt.incrementRetry();
        return executeStep(stepDef, workflowInputs, stepOutputs, workflowExecution);
    }
}
