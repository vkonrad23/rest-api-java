package com.example.tasks.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Partial update (PATCH); only sent fields are applied")
public class TaskPatchRequest {

    @Schema(description = "New title if provided", example = "Buy organic groceries")
    private String title;

    @Schema(description = "New description if provided; empty string clears description")
    private String description;

    @Schema(description = "New completion state if provided", example = "true")
    private Boolean completed;

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

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
