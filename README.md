# Tasks API

Example **Spring Boot** REST service for managing tasks. Data is kept **in memory** (it is lost when the application stops). The API is described with **OpenAPI 3** via [springdoc-openapi](https://springdoc.org/) and exposed in **Swagger UI**.

## Requirements

- **Java 17** or newer  
- **Maven 3.6+**

## Run the application

```bash
mvn spring-boot:run
```

Default base URL: `http://localhost:8080`

## API overview

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/tasks` | List all tasks |
| `GET` | `/api/tasks/{id}` | Get one task by UUID |
| `POST` | `/api/tasks` | Create a task (`title` required) |
| `PUT` | `/api/tasks/{id}` | Replace a task (`title`, `completed` required) |
| `PATCH` | `/api/tasks/{id}` | Partial update (only sent fields change) |
| `DELETE` | `/api/tasks/{id}` | Delete a task (returns `204 No Content`) |

### Task JSON shape

| Field | Type | Notes |
|-------|------|--------|
| `id` | UUID | Set by the server on create |
| `title` | string | Required on create / full update |
| `description` | string | Optional |
| `completed` | boolean | Default `false` on create |
| `createdAt` | instant (ISO-8601) | Read-only |
| `updatedAt` | instant (ISO-8601) | Read-only |

### Example: create and fetch

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy milk","description":"2%","completed":false}'
```

Use the returned `id` in:

```bash
curl -s http://localhost:8080/api/tasks/<id>
```

## OpenAPI and Swagger UI

| Resource | URL |
|----------|-----|
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |

## Tests

The project includes **unit**, **integration** (MockMvc), and **API** (embedded server + `TestRestTemplate`) tests.

```bash
mvn test
```

- **Unit:** `TaskService` logic without Spring (`com.example.tasks.unit`).  
- **Integration:** full servlet stack with MockMvc (`com.example.tasks.integration`).  
- **API:** HTTP calls on a random port (`com.example.tasks.api`).

## Project layout

```
src/main/java/com/example/tasks/
  TasksApplication.java      # Entry point
  config/OpenApiConfig.java  # API title / description in the spec
  dto/                       # Request bodies for POST, PUT, PATCH
  model/Task.java            # Task resource
  service/TaskService.java   # In-memory store
  web/                       # REST controller and exception handling
src/main/resources/
  application.properties     # App name and springdoc paths
src/test/java/               # Unit, integration, and API tests
```

## Build artifact

```bash
mvn -q package
java -jar target/tasks-api-1.0.0-SNAPSHOT.jar
```
