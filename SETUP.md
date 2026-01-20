# AgentFlow Setup Guide

## Quick Start

### 1. Copy environment template
```bash
cp .env.example .env
```

### 2. Add your API keys to `.env`
```
GROQ_API_KEY=gsk_...           # Get FREE key from https://console.groq.com (Recommended)
OPENROUTER_API_KEY=sk-or-v1-... # Get FREE key from https://openrouter.ai (Alternative)
```

### 3. Run the application
```bash
docker-compose up -d
./mvnw spring-boot:run
```

## API Keys

### Groq (FREE) - Recommended
- Get key: https://console.groq.com
- No credit card required
- Fast inference on LPU hardware
- Default model: `openai/gpt-oss-120b`

### OpenRouter (FREE) - Alternative
- Get key: https://openrouter.ai
- No credit card required
- Free models available:
  - `openai/gpt-oss-120b:free`
  - `meta-llama/llama-3.2-3b-instruct:free`

### OpenAI (Paid) - Optional
- Get key: https://platform.openai.com
- Requires credit card
- Models: `gpt-4o-mini`, `gpt-4o`, etc.

### Search
- **DuckDuckGo** - FREE, no API key needed ✅

## Default LLM Configuration

The LLM agent uses these defaults (can be overridden in workflow YAML):
- **Provider**: `groq`
- **Model**: `openai/gpt-oss-120b`
- **Temperature**: `1.0`
- **Top-P**: `1.0`

## Testing(PowerShell)

### Test LLM Agent
```powershell
$body = @{ type = "llm"; config = @{ prompt = "Say hello in 5 words" } } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8080/api/agents/execute -Method POST -Body $body -ContentType "application/json"
```

### Test Search Agent
```powershell
$body = @{ type = "search"; config = @{ query = "AI news 2026"; maxResults = 3 } } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8080/api/agents/execute -Method POST -Body $body -ContentType "application/json"
```

### Test Search + LLM Workflow
```powershell
# Create and execute workflow
$yaml = Get-Content .\test-search-groq.yaml -Raw
Invoke-RestMethod -Uri http://localhost:8080/api/workflows -Method POST -Body $yaml -ContentType "text/plain"

# Get workflow ID and execute
$workflows = Invoke-RestMethod -Uri http://localhost:8080/api/workflows
$wfId = ($workflows.content | Where-Object { $_.name -eq "test-search-groq" }).id
$body = @{ inputs = @{} } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/workflows/$wfId/execute" -Method POST -Body $body -ContentType "application/json"
```

### Swagger UI
For interactive API exploration: http://localhost:8080/swagger-ui.html
