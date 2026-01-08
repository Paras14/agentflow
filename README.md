# AgentFlow

AgentFlow is a workflow orchestration engine designed specifically for building and managing AI agent operations. It provides a reliable, strongly-typed environment to string together LLM calls, data fetching, and processing steps into complex, repeatable workflows.

## Why AgentFlow?
Building reliable AI applications often means chaining together multiple unpredictable steps. AgentFlow brings the stability of the Java/Spring Boot ecosystem to this problem, offering a structured way to define, execute, and monitor agent-based workflows. It handles the "boring" but critical parts like state management, retries, and failure recovery so you can focus on the agent logic.

## Key Features
- **YAML-based Workflows**: Define complex agent interactions in readable, version-controllable YAML files.
- **Built-in Agents**: Ready-to-use integrations for HTTP requests, Database (SQL) access, and LLM providers.
- **Resilient Execution**: Automatic retries, error handling, and persistent state management using PostgreSQL.
- **Scalable**: Built on Spring Boot 3 and designed to scale horizontally.

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven
- Docker (for needed services)

### Quick Start
1. Clone the repository.
2. Start the supporting services (Postgres):
   ```bash
   docker-compose up -d
   ```
3. Build the application:
   ```bash
   ./mvnw clean install
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Workflow Example
Here is what a simple workflow looks like in AgentFlow:

```yaml
name: "research-article"
version: "1.0"
steps:
  - id: "search_topic"
    type: "search"
    inputs:
      query: "${workflow.input.topic}"
  
  - id: "summarize"
    type: "llm"
    inputs:
      prompt: "Summarize the following search results: ${search_topic.output}"
      model: "gpt-4"
```

## Documentation
For more detailed information on architecture and roadmap, please see the `docs/` directory.

## Contributing
This project is open for contributions. Please verify your changes before submitting a pull request.

## License
Distributed under the MIT License. See `LICENSE` for more information.
