package com.java.agentflow.agent.impl;

import com.java.agentflow.agent.Agent;
import com.java.agentflow.agent.AgentCapabilities;
import com.java.agentflow.agent.AgentContext;
import com.java.agentflow.agent.AgentResult;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent for calling LLMs for text generation.
 */
@Component
public class LlmAgent implements Agent {

    private final String openaiKey;
    private final String openrouterKey;
    private final String groqKey;

    public LlmAgent(
            @Value("${openai.api.key:}") String openaiKey,
            @Value("${openrouter.api.key:}") String openrouterKey,
            @Value("${groq.api.key:}") String groqKey) {
        this.openaiKey = openaiKey;
        this.openrouterKey = openrouterKey;
        this.groqKey = groqKey;
    }

    @Override
    public String getType() {
        return "llm";
    }

    @Override
    public AgentCapabilities getCapabilities() {
        return new AgentCapabilities("llm", "Calls LLM (OpenAI, OpenRouter, Groq) for text generation");
    }

    @Override
    public AgentResult execute(AgentContext context) {
        Instant start = Instant.now();

        try {
            String prompt = context.getRequiredConfig("prompt");
            String model = context.getConfig("model", "openai/gpt-oss-120b");
            String provider = context.getConfig("provider", "groq");
            Double temperature = context.getConfig("temperature", 1.0);
            Double topP = context.getConfig("topP", 1.0);
            Integer maxTokens = context.getConfig("maxTokens", 1000);

            String apiKey;
            String baseUrl;

            if ("openrouter".equalsIgnoreCase(provider)) {
                if (openrouterKey == null || openrouterKey.isBlank()) {
                    return AgentResult.failure(
                            "OpenRouter API key not set. Set OPENROUTER_API_KEY environment variable.",
                            Duration.between(start, Instant.now()));
                }
                apiKey = openrouterKey;
                baseUrl = "https://openrouter.ai/api/v1";
            } else if ("groq".equalsIgnoreCase(provider)) {
                if (groqKey == null || groqKey.isBlank()) {
                    return AgentResult.failure(
                            "Groq API key not set. Set GROQ_API_KEY environment variable.",
                            Duration.between(start, Instant.now()));
                }
                apiKey = groqKey;
                baseUrl = "https://api.groq.com/openai/v1";
            } else {
                if (openaiKey == null || openaiKey.isBlank()) {
                    return AgentResult.failure(
                            "OpenAI API key not set. Set OPENAI_API_KEY or use provider: openrouter/groq",
                            Duration.between(start, Instant.now()));
                }
                apiKey = openaiKey;
                baseUrl = null;
            }

            var builder = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(model)
                    .temperature(temperature)
                    .topP(topP)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(60));

            if (baseUrl != null) {
                builder.baseUrl(baseUrl);
            }

            ChatLanguageModel chatModel = builder.build();
            String response = chatModel.generate(prompt);

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("response", response);
            outputs.put("model", model);
            outputs.put("provider", provider);

            return AgentResult.success(outputs, Duration.between(start, Instant.now()));

        } catch (Exception e) {
            return AgentResult.failure("LLM call failed: " + e.getMessage(),
                    Duration.between(start, Instant.now()));
        }
    }
}
