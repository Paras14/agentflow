package com.java.agentflow.agent.impl;

import com.java.agentflow.agent.Agent;
import com.java.agentflow.agent.AgentCapabilities;
import com.java.agentflow.agent.AgentContext;
import com.java.agentflow.agent.AgentResult;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent for web search using DuckDuckGo.
 */
@Component
public class DuckDuckGoSearchAgent implements Agent {

    private final RestTemplate restTemplate;

    public DuckDuckGoSearchAgent() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getType() {
        return "search";
    }

    @Override
    public AgentCapabilities getCapabilities() {
        return new AgentCapabilities("search", "Web search using DuckDuckGo (FREE, no API key needed)");
    }

    @Override
    public AgentResult execute(AgentContext context) {
        Instant start = Instant.now();

        try {
            String query = context.getRequiredConfig("query");
            Integer maxResults = context.getConfig("maxResults", 5);

            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String url = "https://html.duckduckgo.com/html/?q=" + encodedQuery;

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            String html = response.getBody();

            List<Map<String, String>> results = parseResults(html, maxResults);

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("results", results);
            outputs.put("query", query);

            return AgentResult.success(outputs, Duration.between(start, Instant.now()));

        } catch (Exception e) {
            return AgentResult.failure("Search failed: " + e.getMessage(),
                    Duration.between(start, Instant.now()));
        }
    }

    private List<Map<String, String>> parseResults(String html, int maxResults) {
        List<Map<String, String>> results = new ArrayList<>();

        Pattern linkPattern = Pattern.compile("class=\"result__a\"[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>");
        Pattern snippetPattern = Pattern.compile("class=\"result__snippet\">([^<]+)</a>");

        Matcher linkMatcher = linkPattern.matcher(html);
        Matcher snippetMatcher = snippetPattern.matcher(html);

        int count = 0;
        while (linkMatcher.find() && count < maxResults) {
            Map<String, String> result = new HashMap<>();
            result.put("url", linkMatcher.group(1));
            result.put("title", linkMatcher.group(2));

            if (snippetMatcher.find()) {
                result.put("snippet", snippetMatcher.group(1));
            }

            results.add(result);
            count++;
        }

        return results;
    }
}
