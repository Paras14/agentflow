# AgentFlow

AgentFlow is a workflow orchestration engine designed specifically for building and managing AI agent operations. It provides a reliable, strongly-typed environment to string together LLM calls, data fetching, and processing steps into complex, repeatable workflows.

## Why AgentFlow?
Building reliable AI applications often means chaining together multiple unpredictable steps. AgentFlow brings the stability of the Java/Spring Boot ecosystem to this problem, offering a structured way to define, execute, and monitor agent-based workflows. It handles the "boring" but critical parts like state management, retries, and failure recovery so you can focus on the agent logic.

## Key Features
- **YAML-based Workflows**: Define complex agent interactions in readable, version-controllable YAML files.
- **Built-in Agents**: Ready-to-use integrations for HTTP requests, Web Search (DuckDuckGo), and LLM providers (Groq, OpenRouter, OpenAI).
- **Sync & Async Execution**: Run workflows synchronously or queue them for async processing via Kafka.
- **Resilient Execution**: Automatic retries, error handling, and persistent state management using PostgreSQL and Redis.
- **Scalable**: Built on Spring Boot 3 and designed to scale horizontally with Kafka-based message processing.

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven
- Docker (for supporting services)

### Quick Start
1. Clone the repository.
2. Copy `.env.example` to `.env` and add your API keys (see `SETUP.md`).
3. Start the supporting services:
   ```bash
   docker-compose up -d
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Workflow Example
Here is what a simple workflow looks like in AgentFlow:

```yaml
name: research-article
version: "1.0"
steps:
  - id: search_topic
    agent: search
    config:
      query: "latest AI developments"
      maxResults: 3
  
  - id: summarize
    agent: llm
    dependsOn: [search_topic]
    config:
      prompt: "Summarize these results: ${steps['search_topic'].outputs.results}"
```

Execute synchronously or asynchronously:
```bash
# Sync execution (waits for completion)
POST /api/workflows/{id}/execute

# Async execution (returns immediately, processes via Kafka)
POST /api/workflows/{id}/execute?async=true
```

## Documentation
For more detailed information on setup, API keys, and testing, see `SETUP.md`. Architecture details are in the `docs/` directory.

## Contributing
This project is open for contributions. Please verify your changes before submitting a pull request.

## License
Distributed under the MIT License. See `LICENSE` for more information.
