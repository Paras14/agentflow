package com.java.agentflow.agent.impl;

import com.java.agentflow.agent.Agent;
import com.java.agentflow.agent.AgentCapabilities;
import com.java.agentflow.agent.AgentContext;
import com.java.agentflow.agent.AgentResult;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent that makes HTTP requests to external APIs.
 * Supports GET, POST, PUT, DELETE with configurable headers and body.
 */
@Component
public class HttpAgent implements Agent {

    private final RestTemplate restTemplate;

    public HttpAgent() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getType() {
        return "http";
    }

    @Override
    public AgentCapabilities getCapabilities() {
        return new AgentCapabilities(
                "http",
                "Makes HTTP requests to external APIs. Supports GET, POST, PUT, DELETE.");
    }

    @Override
    public AgentResult execute(AgentContext context) {
        Instant start = Instant.now();

        try {
            // Extract configuration
            String url = context.getRequiredConfig("url");
            String method = context.getConfig("method", "GET");
            Map<String, String> headers = context.getConfig("headers", Map.of());
            Object body = context.getConfig("body", null);

            // Build request headers
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            }

            // Build request entity
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, httpHeaders);

            // Execute request
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    httpMethod,
                    requestEntity,
                    Object.class);

            // Build outputs
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("statusCode", response.getStatusCode().value());
            outputs.put("body", response.getBody());
            outputs.put("headers", response.getHeaders().toSingleValueMap());

            Duration executionTime = Duration.between(start, Instant.now());
            return AgentResult.success(outputs, executionTime);

        } catch (RestClientException e) {
            Duration executionTime = Duration.between(start, Instant.now());
            return AgentResult.failure("HTTP request failed: " + e.getMessage(), executionTime);
        } catch (IllegalArgumentException e) {
            Duration executionTime = Duration.between(start, Instant.now());
            return AgentResult.failure("Invalid configuration: " + e.getMessage(), executionTime);
        }
    }
}
