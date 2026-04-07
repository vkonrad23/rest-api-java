package com.example.tasks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for full replacement (PUT)")
public class TaskUpdateRequest {

    @NotBlank
    @Schema(description = "Short title", example = "Buy groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Optional details; omit or null to clear", example = "Milk, bread, eggs")
    private String description;

    @Schema(description = "Completion state", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean completed;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
