package com.java.agentflow.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowProducer {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProducer.class);

    private final KafkaTemplate<String, WorkflowMessage> kafkaTemplate;

    public WorkflowProducer(KafkaTemplate<String, WorkflowMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void queueExecution(UUID workflowId, UUID executionId, Map<String, Object> inputs) {
        WorkflowMessage message = new WorkflowMessage(workflowId, executionId, inputs);

        kafkaTemplate.send(KafkaConfig.WORKFLOW_TOPIC, executionId.toString(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to queue workflow: {}", executionId, ex);
                    } else {
                        log.info("Queued workflow execution: {}", executionId);
                    }
                });
    }
}
