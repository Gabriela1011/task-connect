package com.example.task_connect.controller;

import com.example.task_connect.dto.TaskRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.Task;
import com.example.task_connect.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @DisplayName("POST /api/tasks - Success (201 Created)")
    void createTask_Success() throws Exception {
        TaskRequestDTO dto = createValidDTO();

        Task savedTask = new Task();
        savedTask.setTitle(dto.getTitle());
        ReflectionTestUtils.setField(savedTask, "id", 1L);

        when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(savedTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Reparatie Robinet"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/tasks - Bad Request (400) when title is blank")
    void createTask_BadRequest_Validation() throws Exception {
        TaskRequestDTO dto = createValidDTO();
        dto.setTitle("");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/tasks - Not Found (404) when User is missing")
    void createTask_UserNotFound() throws Exception {
        TaskRequestDTO dto = createValidDTO();

        when(taskService.createTask(any(TaskRequestDTO.class)))
                .thenThrow(new UserNotFoundException("Requester not found"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requester not found"));
    }

    @Test
    @DisplayName("POST /api/tasks - Not Found (404) when Category or Address is missing")
    void createTask_ResourceNotFound() throws Exception {
        TaskRequestDTO dto = createValidDTO();

        when(taskService.createTask(any(TaskRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category or Address not found"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category or Address not found"));
    }


    private TaskRequestDTO createValidDTO() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Reparatie Robinet");
        dto.setBudget(new BigDecimal("150.0"));
        dto.setRequesterId(1L);
        dto.setCategoryId(1L);
        dto.setAddressId(1L);
        return dto;
    }
}