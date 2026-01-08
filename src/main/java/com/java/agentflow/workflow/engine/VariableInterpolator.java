package com.java.agentflow.workflow.engine;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles ${...} variable replacement.
 */
@Component
public class VariableInterpolator {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private static final int MAX_DEPTH = 5;

    public String interpolate(String template, InterpolationContext context) {
        if (template == null || !template.contains("${")) {
            return template;
        }

        return interpolateRecursive(template, context, 0);
    }

    @SuppressWarnings("unchecked")
    public Object interpolateObject(Object value, InterpolationContext context) {
        if (value == null) {
            return null;
        }

        if (value instanceof String str) {
            return interpolate(str, context);
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new java.util.HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(entry.getKey().toString(), interpolateObject(entry.getValue(), context));
            }
            return result;
        }

        if (value instanceof java.util.List<?> list) {
            return list.stream()
                    .map(item -> interpolateObject(item, context))
                    .toList();
        }

        return value;
    }

    private String interpolateRecursive(String template, InterpolationContext context, int depth) {
        if (depth >= MAX_DEPTH) {
            throw new InterpolationException("Max interpolation depth exceeded");
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            Object value = resolveExpression(expression, context);
            String replacement = valueToString(value);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        String interpolated = result.toString();

        if (interpolated.contains("${") && !interpolated.equals(template)) {
            return interpolateRecursive(interpolated, context, depth + 1);
        }

        return interpolated;
    }

    /**
     * Resolve an expression like "inputs.apiUrl" or
     * "steps['fetch-data'].outputs.body"
     */
    private Object resolveExpression(String expression, InterpolationContext context) {
        try {
            // Parse the expression
            if (expression.startsWith("inputs.") || expression.equals("inputs")) {
                return resolveInputs(expression, context.inputs());
            }

            if (expression.startsWith("steps[") || expression.startsWith("steps.")) {
                return resolveStepOutput(expression, context.stepOutputs());
            }

            // Direct input reference
            if (context.inputs().containsKey(expression)) {
                return context.inputs().get(expression);
            }

            throw new InterpolationException("Unknown variable: " + expression);

        } catch (InterpolationException e) {
            throw e;
        } catch (Exception e) {
            throw new InterpolationException("Failed to resolve: " + expression + " - " + e.getMessage());
        }
    }

    private Object resolveInputs(String expression, Map<String, Object> inputs) {
        if (expression.equals("inputs")) {
            return inputs;
        }

        String path = expression.substring("inputs.".length());
        return resolvePath(inputs, path);
    }

    private Object resolveStepOutput(String expression, Map<String, Map<String, Object>> stepOutputs) {
        String remaining;
        String stepId;

        if (expression.startsWith("steps['")) {
            int endBracket = expression.indexOf("']");
            if (endBracket == -1) {
                throw new InterpolationException("Invalid step reference: " + expression);
            }
            stepId = expression.substring("steps['".length(), endBracket);
            remaining = expression.substring(endBracket + 2);
            if (remaining.startsWith(".")) {
                remaining = remaining.substring(1);
            }
        } else {
            // steps.stepId format
            remaining = expression.substring("steps.".length());
            int dotIndex = remaining.indexOf('.');
            if (dotIndex == -1) {
                stepId = remaining;
                remaining = "";
            } else {
                stepId = remaining.substring(0, dotIndex);
                remaining = remaining.substring(dotIndex + 1);
            }
        }

        Map<String, Object> stepOutput = stepOutputs.get(stepId);
        if (stepOutput == null) {
            throw new InterpolationException("Step not found or not yet executed: " + stepId);
        }

        if (remaining.isEmpty()) {
            return stepOutput;
        }

        return resolvePath(stepOutput, remaining);
    }

    @SuppressWarnings("unchecked")
    private Object resolvePath(Map<String, Object> root, String path) {
        String[] parts = path.split("\\.");
        Object current = root;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                throw new InterpolationException("Cannot access '" + part + "' on non-object value");
            }
        }

        return current;
    }

    private String valueToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    /**
     * Context for variable interpolation.
     */
    public record InterpolationContext(
            Map<String, Object> inputs,
            Map<String, Map<String, Object>> stepOutputs) {
        public InterpolationContext {
            inputs = inputs != null ? inputs : Map.of();
            stepOutputs = stepOutputs != null ? stepOutputs : Map.of();
        }

        public static InterpolationContext empty() {
            return new InterpolationContext(Map.of(), Map.of());
        }
    }
}
