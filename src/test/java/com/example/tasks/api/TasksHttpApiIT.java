package com.example.tasks.api;

import com.example.tasks.TasksApplication;
import com.example.tasks.model.Task;
import com.example.tasks.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TasksApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TasksHttpApiIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void resetStore() {
        taskService.clearAll();
    }

    @Test
    void crudFlow_overRealHttp() {
        ResponseEntity<List<Task>> empty = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).isNotNull().isEmpty();

        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks",
                Map.of("title", "HTTP task", "description", "via TestRestTemplate", "completed", false),
                Task.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        UUID id = created.getBody().getId();
        assertThat(created.getBody().getTitle()).isEqualTo("HTTP task");

        ResponseEntity<Task> fetched = restTemplate.getForEntity("/api/tasks/{id}", Task.class, id);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getId()).isEqualTo(id);

        ResponseEntity<Task> put = restTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Updated", "description", "d2", "completed", true)),
                Task.class,
                id
        );
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put.getBody()).isNotNull();
        assertThat(put.getBody().isCompleted()).isTrue();

        ResponseEntity<Task> patched = restTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("completed", false)),
                Task.class,
                id
        );
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patched.getBody()).isNotNull();
        assertThat(patched.getBody().isCompleted()).isFalse();

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                id
        );
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> gone = restTemplate.getForEntity("/api/tasks/{id}", String.class, id);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void openApiJson_isServed() {
        ResponseEntity<String> docs = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(docs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(docs.getBody()).isNotNull();
        assertThat(docs.getBody()).contains("Tasks");
        assertThat(docs.getBody()).contains("/api/tasks");
        assertThat(docs.getBody()).contains("/api/hypermedia/tasks");
    }

    @Test
    void list_supportsCompletedFilteringOverHttp() {
        restTemplate.postForEntity(
                "/api/tasks",
                Map.of("title", "Done task", "description", "done", "completed", true),
                Task.class
        );
        restTemplate.postForEntity(
                "/api/tasks",
                Map.of("title", "Open task", "description", "open", "completed", false),
                Task.class
        );

        ResponseEntity<List<Task>> all = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotNull().hasSize(2);

        ResponseEntity<List<Task>> completedOnly = restTemplate.exchange(
                "/api/tasks?completed=true",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(completedOnly.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completedOnly.getBody()).isNotNull().hasSize(1);
        assertThat(completedOnly.getBody().get(0).isCompleted()).isTrue();

        ResponseEntity<List<Task>> incompleteOnly = restTemplate.exchange(
                "/api/tasks?completed=false",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(incompleteOnly.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(incompleteOnly.getBody()).isNotNull().hasSize(1);
        assertThat(incompleteOnly.getBody().get(0).isCompleted()).isFalse();
    }

    @Test
    void createAndReplace_validateTitleConstraints() {
        ResponseEntity<String> invalidCreate = restTemplate.postForEntity(
                "/api/tasks",
                Map.of("title", "ab", "description", "too short", "completed", false),
                String.class
        );
        assertThat(invalidCreate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidCreate.getBody()).contains("between 3 and 100");

        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks",
                Map.of("title", "Valid title", "description", "ok", "completed", false),
                Task.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();

        UUID id = created.getBody().getId();
        ResponseEntity<String> invalidPut = restTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "  ", "description", "d", "completed", true)),
                String.class,
                id
        );
        assertThat(invalidPut.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidPut.getBody()).contains("must not be blank");

        ResponseEntity<Task> validPut = restTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("title", "Updated title", "description", "d2", "completed", true)),
                Task.class,
                id
        );
        assertThat(validPut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validPut.getBody()).isNotNull();
        assertThat(validPut.getBody().getTitle()).isEqualTo("Updated title");
    }
}
