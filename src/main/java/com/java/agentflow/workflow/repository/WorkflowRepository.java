package com.java.agentflow.workflow.repository;

import com.java.agentflow.workflow.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Workflow entities.
 * Uses Spring Data JPA for type-safe, parameterized queries.
 */
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    /**
     * Find a workflow by name and version.
     */
    Optional<Workflow> findByNameAndVersion(String name, String version);

    /**
     * Find the latest version of a workflow by name.
     */
    Optional<Workflow> findFirstByNameOrderByVersionDesc(String name);

    /**
     * Check if a workflow with the given name and version exists.
     */
    boolean existsByNameAndVersion(String name, String version);
}
