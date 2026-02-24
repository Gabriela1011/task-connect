package com.example.task_connect.controller;

import com.example.task_connect.dto.BidRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.model.Bid;
import com.example.task_connect.service.BidService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BidController.class)
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BidService bidService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @DisplayName("POST /api/tasks/{taskId}/bids - Success (201 Created)")
    void submitBid_Success() throws Exception {
        Long taskId = 1L;
        BidRequestDTO dto = createValidBidDTO();

        Bid savedBid = new Bid();
        savedBid.setAmount(dto.getAmount());
        ReflectionTestUtils.setField(savedBid, "id", 100L);

        when(bidService.submitBid(eq(taskId), any(BidRequestDTO.class))).thenReturn(savedBid);

        mockMvc.perform(post("/api/tasks/{taskId}/bids", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.amount").value(150.0));
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/bids - 400 Bad Request (Validation Error)")
    void submitBid_ValidationError() throws Exception {
        BidRequestDTO invalidDto = createValidBidDTO();
        invalidDto.setAmount(new BigDecimal("-10.0"));

        mockMvc.perform(post("/api/tasks/1/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/bids - 400 Bad Request (Business Logic Error)")
    void submitBid_IllegalState() throws Exception {
        BidRequestDTO dto = createValidBidDTO();

        when(bidService.submitBid(eq(1L), any(BidRequestDTO.class)))
                .thenThrow(new IllegalStateException("You cannot bid on your own task."));

        mockMvc.perform(post("/api/tasks/1/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot bid on your own task."));
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/bids - 404 Not Found")
    void submitBid_NotFound() throws Exception {
        BidRequestDTO dto = createValidBidDTO();

        when(bidService.submitBid(eq(99L), any(BidRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Task not found with ID: 99"));

        mockMvc.perform(post("/api/tasks/99/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with ID: 99"));
    }


    private BidRequestDTO createValidBidDTO() {
        BidRequestDTO dto = new BidRequestDTO();
        dto.setAmount(new BigDecimal("150.0"));
        dto.setMessage("Pot repara robinetul rapid.");
        dto.setTaskerId(2L);
        return dto;
    }
}