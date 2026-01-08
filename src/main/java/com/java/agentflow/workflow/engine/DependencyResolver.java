package com.java.agentflow.workflow.engine;

import com.java.agentflow.workflow.model.WorkflowDefinition.StepDefinition;

import java.util.*;

/**
 * Sorts steps based on their dependencies.
 */
public class DependencyResolver {

    /**
     * returns steps in execution order
     */
    public List<StepDefinition> resolve(List<StepDefinition> steps) {
        // build graph
        Map<String, StepDefinition> stepMap = new HashMap<>();
        Map<String, Set<String>> dependents = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (StepDefinition step : steps) {
            stepMap.put(step.id(), step);
            dependents.put(step.id(), new HashSet<>());
            inDegree.put(step.id(), 0);
        }

        // Build dependency graph
        for (StepDefinition step : steps) {
            if (step.dependsOn() != null) {
                for (String dep : step.dependsOn()) {
                    if (!stepMap.containsKey(dep)) {
                        throw new IllegalStateException("Unknown dependency: " + dep + " in step: " + step.id());
                    }
                    dependents.get(dep).add(step.id());
                    inDegree.merge(step.id(), 1, Integer::sum);
                }
            }
        }

        // sort
        Queue<String> ready = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                ready.add(entry.getKey());
            }
        }

        List<StepDefinition> sorted = new ArrayList<>();

        while (!ready.isEmpty()) {
            String stepId = ready.poll();
            sorted.add(stepMap.get(stepId));

            for (String dependent : dependents.get(stepId)) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);
                if (newDegree == 0) {
                    ready.add(dependent);
                }
            }
        }

        // check circular dependency
        if (sorted.size() != steps.size()) {
            throw new IllegalStateException("Circular dependency detected");
        }

        return sorted;
    }

    /**
     * Get steps that can be executed in parallel (same level in DAG).
     * Returns groups of steps that can run concurrently.
     */
    public List<List<StepDefinition>> getExecutionLevels(List<StepDefinition> steps) {
        List<StepDefinition> sorted = resolve(steps);

        Map<String, Integer> levels = new HashMap<>();

        for (StepDefinition step : sorted) {
            int level = 0;
            if (step.dependsOn() != null) {
                for (String dep : step.dependsOn()) {
                    level = Math.max(level, levels.getOrDefault(dep, 0) + 1);
                }
            }
            levels.put(step.id(), level);
        }

        // Group by level
        Map<Integer, List<StepDefinition>> levelGroups = new TreeMap<>();
        for (StepDefinition step : sorted) {
            int level = levels.get(step.id());
            levelGroups.computeIfAbsent(level, k -> new ArrayList<>()).add(step);
        }

        return new ArrayList<>(levelGroups.values());
    }
}
