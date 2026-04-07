package com.example.tasks.web;

import com.example.tasks.dto.TaskCreateRequest;
import com.example.tasks.dto.TaskPatchRequest;
import com.example.tasks.dto.TaskUpdateRequest;
import com.example.tasks.model.Task;
import com.example.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Create, read, update, and delete tasks (in-memory store)")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "List all tasks")
    @ApiResponse(
            responseCode = "200",
            description = "All tasks",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Task.class))
            )
    )
    public List<Task> list() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Found",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Task get(
            @Parameter(description = "Task id", required = true) @PathVariable UUID id) {
        return taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task")
    @ApiResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(schema = @Schema(implementation = Task.class))
    )
    public Task create(@Valid @RequestBody TaskCreateRequest body) {
        return taskService.create(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a task (full update)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Task replace(
            @Parameter(description = "Task id", required = true) @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest body) {
        return taskService.replace(id, body).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a task")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Task patch(
            @Parameter(description = "Task id", required = true) @PathVariable UUID id,
            @Valid @RequestBody TaskPatchRequest body) {
        return taskService.patch(id, body).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public void delete(@Parameter(description = "Task id", required = true) @PathVariable UUID id) {
        if (!taskService.delete(id)) {
            throw new TaskNotFoundException(id);
        }
    }
}
