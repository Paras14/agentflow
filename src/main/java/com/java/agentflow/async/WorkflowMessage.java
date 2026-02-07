package com.java.agentflow.async;

import java.util.Map;
import java.util.UUID;

public record WorkflowMessage(
        UUID workflowId,
        UUID executionId,
        Map<String, Object> inputs) {
}
