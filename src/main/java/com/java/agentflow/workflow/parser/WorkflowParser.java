package com.java.agentflow.workflow.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.java.agentflow.workflow.model.WorkflowDefinition;
import com.java.agentflow.workflow.model.WorkflowDefinition.RetryConfig;
import com.java.agentflow.workflow.model.WorkflowDefinition.StepDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses workflow YAML definitions.
 */
@Component
public class WorkflowParser {

    private static final int MAX_YAML_SIZE = 1024 * 100; // 100KB
    private static final int MAX_STEPS = 100;

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public WorkflowParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.jsonMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public WorkflowDefinition parseYaml(String yaml) {
        if (yaml == null || yaml.isBlank()) {
            throw new WorkflowParseException("Workflow YAML cannot be empty");
        }

        // limit size
        if (yaml.length() > MAX_YAML_SIZE) {
            throw new WorkflowParseException("Workflow YAML too large");
        }

        try {
            Map<String, Object> rawWorkflow = yamlMapper.readValue(
                    yaml,
                    new TypeReference<Map<String, Object>>() {
                    });

            return parseFromMap(rawWorkflow);

        } catch (WorkflowParseException e) {
            throw e;
        } catch (Exception e) {
            throw new WorkflowParseException("Failed to parse workflow YAML: " + e.getMessage(), e);
        }
    }

    /**
     * Parse a workflow from a Map (used for JSONB deserialization).
     */
    public WorkflowDefinition parseFromMap(Map<String, Object> definition) {
        String name = getString(definition, "name");
        String version = getStringOrDefault(definition, "version", "1.0");
        String description = getStringOrDefault(definition, "description", null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawSteps = (List<Map<String, Object>>) definition.get("steps");

        if (rawSteps == null || rawSteps.isEmpty()) {
            throw new WorkflowParseException("No steps found");
        }

        if (rawSteps.size() > MAX_STEPS) {
            throw new WorkflowParseException("Too many steps (max " + MAX_STEPS + ")");
        }

        List<StepDefinition> steps = new ArrayList<>();
        for (Map<String, Object> rawStep : rawSteps) {
            steps.add(parseStep(rawStep));
        }

        WorkflowDefinition workflow = new WorkflowDefinition(name, version, description, steps);
        workflow.validate();

        return workflow;
    }

    private StepDefinition parseStep(Map<String, Object> rawStep) {
        String id = getString(rawStep, "id");
        String agent = getString(rawStep, "agent");

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) rawStep.getOrDefault("config", Map.of());

        @SuppressWarnings("unchecked")
        List<String> dependsOn = (List<String>) rawStep.get("dependsOn");

        RetryConfig retry = parseRetryConfig(rawStep.get("retry"));

        return new StepDefinition(id, agent, config, dependsOn, retry);
    }

    private RetryConfig parseRetryConfig(Object retryObj) {
        if (retryObj == null) {
            return RetryConfig.defaultConfig();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> retryMap = (Map<String, Object>) retryObj;

        int maxRetries = getIntOrDefault(retryMap, "maxRetries", 3);
        long delayMs = getLongOrDefault(retryMap, "delayMs", 1000);

        return new RetryConfig(maxRetries, delayMs);
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new WorkflowParseException("Missing required field: " + key);
        }
        return value.toString();
    }

    private String getStringOrDefault(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getIntOrDefault(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Number)
            return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private long getLongOrDefault(Map<String, Object> map, String key, long defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Number)
            return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    /**
     * Convert a WorkflowDefinition to a Map for JSONB storage.
     */
    public Map<String, Object> toMap(WorkflowDefinition definition) {
        return jsonMapper.convertValue(definition, new TypeReference<Map<String, Object>>() {
        });
    }
}
