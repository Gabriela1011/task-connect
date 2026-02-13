package com.example.task_connect.controller;

import com.example.task_connect.dto.TaskRequestDTO;
import com.example.task_connect.model.Task;
import com.example.task_connect.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "Endpoint dor posting a task")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
            summary = "Post a new task",
            description = "Allows a registered user to post a task."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task successfully posted"),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing fields)"),
            @ApiResponse(responseCode = "404", description = "Requester, Category, or Address ID not found")
    })
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequestDTO taskRequest) {
        Task createdTask = taskService.createTask(taskRequest);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
}
