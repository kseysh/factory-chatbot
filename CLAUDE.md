# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**factory-chatbot** is a Spring Boot-based AI chatbot application that integrates with Large Language Models (LLMs) using the Model Context Protocol (MCP). It's structured as a Gradle multi-module project with three main modules:

- **chat**: REST API server that handles chat requests
- **mcp-client**: Spring AI integration for LLM communication
- **mcp-db-server**: MCP server for database access (skeleton implementation)

**Tech Stack:**
- Java 21
- Spring Boot 3.5.6
- Spring AI 1.0.3 (MCP protocol support)
- Gradle (multi-module)
- LLM: Ollama (default), AWS Bedrock (optional)

## Common Commands

### Build & Test
```bash
# Build all modules
./gradlew build

# Build without running tests (used in CI/CD)
./gradlew build -x test

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :chat:test
./gradlew :mcp-client:test

# Run a single test class
./gradlew :chat:test --tests ChatControllerTest

# Run a specific test method
./gradlew :chat:test --tests ChatControllerTest.testChatEndpoint
```

### Development
```bash
# Start the chat application (main entry point)
./gradlew :chat:bootRun

# Start mcp-client application
./gradlew :mcp-client:bootRun

# Clean build artifacts
./gradlew clean

# Check dependencies (shows dependency tree)
./gradlew dependencies

# Run Gradle with verbose output for debugging
./gradlew build -i
```

### Project Structure Commands
```bash
# List all Gradle tasks available
./gradlew tasks

# Get build info for a module
./gradlew :chat:buildEnvironment
```

## Architecture & Design

### Module Dependency Graph
```
chat (Spring Boot REST API)
  ↓ depends on
mcp-client (Spring AI + LLM Integration)
  ↓ provides
mcp-db-server (Standalone MCP Server, not yet integrated)
```

### Key Architectural Patterns

#### 1. **Component Scanning with @ComponentScan**
The `ChatApplication` class uses `@ComponentScan` to explicitly include both `core.mcpclient` and `core.chat` packages:
```java
@ComponentScan(basePackages = {"core.mcpclient, core.chat"})
```
This is crucial for dependency injection across modules. If you add new components, ensure they're in one of these scanned packages.

#### 2. **Spring AI Integration**
- **LLMConfig** (`mcp-client/src/main/java/core/mcpclient/config/LLMConfig.java`): Creates a `ChatClient` bean from the auto-configured `ChatModel`
- **LLMService** (`mcp-client/src/main/java/core/mcpclient/service/LLMService.java`): Wraps ChatClient with error handling
- The ChatModel is auto-configured by Spring AI starter dependencies based on `application.yml` profile

#### 3. **Controller-Service Pattern**
- **ChatController** (`chat/src/main/java/core/chat/controller/ChatController.java`): Handles 3 endpoints
  - `POST /v1/chat`: Chat functionality (delegates to ChatService)
  - `GET /v1/mcp/health`: LLM health check
  - `GET /health`: Application health check
- **ChatService** (`chat/src/main/java/core/chat/service/ChatService.java`): Business logic for chat processing
- **LLMHealthCheckService** (`mcp-client/src/main/java/core/mcpclient/service/LLMHealthCheckService.java`): Validates LLM connectivity

### Request/Response Models
- **ChatRequest**: Located in `chat/src/main/java/core/chat/controller/request/ChatRequest.java`
- **ChatResponse**: Located in `chat/src/main/java/core/chat/controller/response/ChatResponse.java`

These DTOs are used for REST API serialization/deserialization.

### Configuration Management

Each module has environment-specific configuration files:

**chat module:**
- `application.yml`: Base configuration (app name: "chat")
- `application-local.yml`: Local development overrides
- `application-prod.yml`: Production config (loaded from GitHub Secrets in CI)

**mcp-client module:**
- `application.yml`: Base configuration (app name: "mcp-client")
- `application-prod.yml`: Production config (loaded from GitHub Secrets in CI)

**mcp-db-server module:**
- `application.yml`: Basic configuration only

### CI/CD Pipeline

**Trigger:** Pull requests to the `main` branch
**Workflow file:** `.github/workflows/ci-prod.yml`

**Pipeline stages:**
1. Checkout source code
2. Setup JDK 21 (Corretto distribution)
3. Inject prod config files from GitHub Secrets:
   - `CHAT_APPLICATION_PROD_YML` → `chat/src/main/resources/application-prod.yml`
   - `MCP_CLIENT_APPLICATION_PROD_YML` → `mcp-client/src/main/resources/application-prod.yml`
4. Grant execute permission to `gradlew`
5. Build all modules with `./gradlew build -x test`

**Important:** Production YML files are not in the repository for security reasons. They must be configured as GitHub repository secrets.

## Development Notes

### Lombok Usage
The project uses Lombok extensively for reducing boilerplate. Common annotations used:
- `@RequiredArgsConstructor`: Generates constructor for final fields (enables constructor injection)
- `@Slf4j`: Adds a `log` field for SLF4J logging
- `@Data`: Generates getters, setters, equals, hashCode, toString

Ensure the Lombok annotation processor is properly configured in your IDE.

### Spring AI ChatClient
The `ChatClient` API is fluent and simple:
```java
chatClient.prompt(question).call().content()
```
Refer to Spring AI documentation for advanced features like streaming, templates, and function calling.

### Error Handling
Currently, `LLMService.chat()` catches all exceptions and returns the exception string. Consider implementing proper error handling with HTTP status codes for production use.

### Testing Patterns
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller unit tests
- Mock `LLMService` or `ChatClient` for testing without an actual LLM

## Gradle & Dependency Management

### Multi-Module Build
All modules share configuration through `allprojects` block in root `build.gradle`:
- Java 21 compatibility enforced
- Spring AI BOM imported for consistent versions
- Lombok annotation processors configured
- JUnit Platform configured for test execution

### Dependency Scope Explanation
```
- implementation: Used only in this module's implementation
- api: Exported to modules that depend on this module
- compileOnly: Compile-time only, not included in artifacts
- annotationProcessor: Used during compilation for code generation
```

For `mcp-client`, dependencies are:
- `spring-ai-starter-mcp-client`: MCP protocol client
- `spring-ai-starter-model-ollama`: Ollama LLM support (default)
- `spring-ai-starter-model-bedrock`: AWS Bedrock support (commented out)

## Key Directories & Files

```
factory-chatbot/
├── chat/                                    # REST API module
│   ├── src/main/java/core/chat/
│   │   ├── ChatApplication.java            # Entry point (ComponentScan important!)
│   │   ├── controller/
│   │   │   ├── ChatController.java         # 3 REST endpoints
│   │   │   ├── request/ChatRequest.java
│   │   │   └── response/ChatResponse.java
│   │   └── service/ChatService.java        # Business logic
│   ├── build.gradle                        # chat module dependencies
│   └── src/main/resources/
│       ├── application.yml                 # Base config (app name)
│       ├── application-local.yml           # .gitignored local override
│       └── application-prod.yml            # .gitignored prod config
├── mcp-client/                             # Spring AI module
│   ├── src/main/java/core/mcpclient/
│   │   ├── McpClientApplication.java       # Standalone app (rarely used)
│   │   ├── config/LLMConfig.java           # ChatClient bean factory
│   │   └── service/
│   │       ├── LLMService.java             # ChatClient wrapper
│   │       └── LLMHealthCheckService.java  # LLM connectivity check
│   ├── build.gradle                        # Spring AI dependencies
│   └── src/main/resources/
│       ├── application.yml                 # Base config (app name)
│       └── application-prod.yml            # .gitignored prod config
├── mcp-db-server/                          # MCP server module (skeleton)
│   ├── src/main/java/core/mcpdbserver/
│   │   └── McpDbServerApplication.java     # Empty skeleton
│   ├── build.gradle                        # MCP Server dependency only
│   └── src/main/resources/application.yml
├── build.gradle                            # Root multi-module config
├── settings.gradle                         # Module definitions
├── .github/workflows/
│   └── ci-prod.yml                         # GitHub Actions CI/CD
└── .gitignore                              # Ignores local/prod configs
```

## Git & Branch Strategy

**Current branch:** `feat/#11-add-ci-flow` (CI/CD pipeline implementation)
**Main branch:** `main` (production-ready code)

Recent work focuses on establishing proper CI/CD automation. Feature branches should:
1. Create PR against `main`
2. CI pipeline will automatically validate the build
3. Merge after passing all checks

## Common Issues & Solutions

### Issue: Tests skip during build
**Solution:** This is intentional in CI (`./gradlew build -x test`). Run `./gradlew test` locally to execute tests.

### Issue: ChatClient bean not found
**Solution:** Check that:
1. Spring AI starter dependency is in `mcp-client/build.gradle`
2. LLMConfig is in a package scanned by ChatApplication
3. application.yml has proper LLM provider configuration (Ollama or Bedrock)

### Issue: MCP-related classes not available
**Solution:** Ensure `spring-ai-starter-mcp-client` dependency is present and properly versioned via BOM.

## Configuration Profiles

Run with different profiles:
```bash
# Use local config (default for development)
./gradlew :chat:bootRun --args='--spring.profiles.active=local'

# Use prod config (requires GitHub Secrets setup)
./gradlew :chat:bootRun --args='--spring.profiles.active=prod'
```

Profile resolution order: `application.yml` → `application-{profile}.yml` (overwrites)