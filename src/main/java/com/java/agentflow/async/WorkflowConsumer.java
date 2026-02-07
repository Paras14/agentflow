package com.java.agentflow.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class WorkflowConsumer {

    private static final Logger log = LoggerFactory.getLogger(WorkflowConsumer.class);
    private static final int MAX_RETRIES = 3;

    private final AsyncExecutionService executionService;
    private final ExecutionStateService stateService;
    private final KafkaTemplate<String, WorkflowMessage> kafkaTemplate;

    public WorkflowConsumer(
            AsyncExecutionService executionService,
            ExecutionStateService stateService,
            KafkaTemplate<String, WorkflowMessage> kafkaTemplate) {
        this.executionService = executionService;
        this.stateService = stateService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaConfig.WORKFLOW_TOPIC, groupId = "agentflow-workers", containerFactory = "kafkaListenerContainerFactory")
    public void handleExecution(WorkflowMessage message, Acknowledgment ack) {
        log.info("Processing workflow: {}", message.executionId());

        if (!stateService.tryLock(message.executionId())) {
            log.warn("Execution already being processed: {}", message.executionId());
            ack.acknowledge();
            return;
        }

        try {
            executionService.executeWorkflow(message.workflowId(), message.executionId(), message.inputs());
            ack.acknowledge();
        } catch (Exception e) {
            // If execution or workflow no longer exists, just acknowledge and skip
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                log.warn("Skipping deleted execution: {} - {}", message.executionId(), e.getMessage());
                ack.acknowledge();
                return;
            }
            log.error("Execution failed: {}", message.executionId(), e);
            handleFailure(message, e, ack);
        } finally {
            stateService.unlock(message.executionId());
        }
    }

    private void handleFailure(WorkflowMessage message, Exception e, Acknowledgment ack) {
        int retryCount = stateService.incrementRetry(message.executionId());

        if (retryCount >= MAX_RETRIES) {
            log.error("Max retries reached, sending to DLQ: {}", message.executionId());
            kafkaTemplate.send(KafkaConfig.DLQ_TOPIC, message.executionId().toString(), message);
            executionService.markExecutionFailed(message.executionId(), e.getMessage());
        }

        ack.acknowledge();
    }
}
