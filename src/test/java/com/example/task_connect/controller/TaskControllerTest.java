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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    //PATCH

    @Test
    @DisplayName("PATCH /api/tasks/{taskId}/bids/{bidId} - Success (200 OK)")
    void acceptBid_Success() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/bids/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Collaboration started! Task assigned and bid accepted."));

        verify(taskService, times(1)).acceptBid(1L, 5L);
    }

    @Test
    @DisplayName("PATCH /api/tasks/{taskId}/bids/{bidId} - 400 Bad Request (Illegal State)")
    void acceptBid_TaskAlreadyAssigned() throws Exception {
        doThrow(new IllegalStateException("Task is ASSIGNED. Only OPEN tasks can accept bids."))
                .when(taskService).acceptBid(anyLong(), anyLong());

        mockMvc.perform(patch("/api/tasks/1/bids/5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task is ASSIGNED. Only OPEN tasks can accept bids."));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{taskId}/bids/{bidId} - 404 Not Found")
    void acceptBid_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Bid not found with ID: 5"))
                .when(taskService).acceptBid(anyLong(), anyLong());

        mockMvc.perform(patch("/api/tasks/1/bids/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bid not found with ID: 5"));
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