package com.java.agentflow.agent.impl;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.agentflow.agent.Agent;
import com.java.agentflow.agent.AgentCapabilities;
import com.java.agentflow.agent.AgentContext;
import com.java.agentflow.agent.AgentResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent that transforms data using JSONPath-like expressions.
 * Useful for extracting and restructuring data between workflow steps.
 */
@Component
public class TransformAgent implements Agent {

    private final ObjectMapper objectMapper;

    public TransformAgent() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getType() {
        return "transform";
    }

    @Override
    public AgentCapabilities getCapabilities() {
        return new AgentCapabilities(
                "transform",
                "Transforms data using JSON Pointer expressions. Extract and restructure data.");
    }

    @Override
    public AgentResult execute(AgentContext context) {
        Instant start = Instant.now();

        try {
            // Get the input data to transform
            Object inputData = context.inputs().get("data");
            if (inputData == null) {
                inputData = context.inputs();
            }

            // Get the expression (JSON Pointer format, e.g., "/body/title")
            String expression = context.getConfig("expression", null);

            // Get optional output mappings
            Map<String, String> mappings = context.getConfig("mappings", null);

            Map<String, Object> outputs = new HashMap<>();

            // Convert input to JsonNode for processing
            JsonNode rootNode = objectMapper.valueToTree(inputData);

            if (expression != null) {
                // Single expression mode - extract one value
                JsonNode result = extractValue(rootNode, expression);
                outputs.put("result", nodeToValue(result));
            } else if (mappings != null) {
                // Mapping mode - extract multiple values
                for (Map.Entry<String, String> mapping : mappings.entrySet()) {
                    String outputKey = mapping.getKey();
                    String path = mapping.getValue();
                    JsonNode result = extractValue(rootNode, path);
                    outputs.put(outputKey, nodeToValue(result));
                }
            } else {
                // Pass-through mode
                outputs.put("result", inputData);
            }

            Duration executionTime = Duration.between(start, Instant.now());
            return AgentResult.success(outputs, executionTime);

        } catch (Exception e) {
            Duration executionTime = Duration.between(start, Instant.now());
            return AgentResult.failure("Transform failed: " + e.getMessage(), executionTime);
        }
    }

    private JsonNode extractValue(JsonNode root, String expression) {
        // Support both "/path/to/value" and "path.to.value" formats
        String pointer = expression.startsWith("/")
                ? expression
                : "/" + expression.replace(".", "/");

        return root.at(JsonPointer.compile(pointer));
    }

    private Object nodeToValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isArray() || node.isObject()) {
            return objectMapper.convertValue(node, Object.class);
        }
        return node.asText();
    }
}
