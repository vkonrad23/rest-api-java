package com.example.tasks.integration;

import com.example.tasks.TasksApplication;
import com.example.tasks.dto.TaskCreateRequest;
import com.example.tasks.dto.TaskPatchRequest;
import com.example.tasks.dto.TaskUpdateRequest;
import com.example.tasks.model.Task;
import com.example.tasks.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TasksApplication.class)
@AutoConfigureMockMvc
class TasksMockMvcIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void resetStore() {
        taskService.clearAll();
    }

    @Test
    void getAll_initiallyEmpty() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void post_createReturns201AndBody() throws Exception {
        TaskCreateRequest body = new TaskCreateRequest();
        body.setTitle("Learn Spring");
        body.setDescription("REST basics");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Learn Spring"))
                .andExpect(jsonPath("$.description").value("REST basics"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void post_invalidTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .containsIgnoringCase("not found"));
    }

    @Test
    void put_replaceAndGet_roundTrip() throws Exception {
        String id = objectMapper.readTree(
                mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq("A", null, false))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asText();

        TaskUpdateRequest upd = new TaskUpdateRequest();
        upd.setTitle("B");
        upd.setDescription("full");
        upd.setCompleted(true);

        mockMvc.perform(put("/api/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("B"))
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("B"));
    }

    @Test
    void patch_partialUpdate() throws Exception {
        String id = objectMapper.readTree(
                mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq("T", "d", false))))
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asText();

        TaskPatchRequest patch = new TaskPatchRequest();
        patch.setCompleted(true);

        mockMvc.perform(patch("/api/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void delete_returns204() throws Exception {
        Task created = objectMapper.readValue(
                mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq("tmp", null, false))))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                Task.class
        );

        mockMvc.perform(delete("/api/tasks/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    private static TaskCreateRequest createReq(String title, String description, boolean completed) {
        TaskCreateRequest r = new TaskCreateRequest();
        r.setTitle(title);
        r.setDescription(description);
        r.setCompleted(completed);
        return r;
    }
}
