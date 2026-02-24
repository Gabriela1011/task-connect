package com.example.task_connect.service;

import com.example.task_connect.dto.BidRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.*;
import com.example.task_connect.model.enums.TaskStatus;
import com.example.task_connect.repository.BidRepository;
import com.example.task_connect.repository.TaskRepository;
import com.example.task_connect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BidService bidService;

    private BidRequestDTO bidDto;
    private Task task;
    private User requester;
    private User tasker;

    @BeforeEach
    void setUp() {
        bidDto = new BidRequestDTO();
        bidDto.setAmount(new BigDecimal("200.00"));
        bidDto.setMessage("Oferta mea");
        bidDto.setTaskerId(2L);

        requester = new User();
        ReflectionTestUtils.setField(requester, "id", 1L);

        tasker = new User();
        ReflectionTestUtils.setField(tasker, "id", 2L);

        task = new Task();
        ReflectionTestUtils.setField(task, "id", 10L);
        task.setRequester(requester);
    }

    @Test
    @DisplayName("Should successfully save bid when all conditions are met")
    void submitBid_Success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tasker));
        when(bidRepository.save(any(Bid.class))).thenAnswer(i -> i.getArgument(0));

        Bid result = bidService.submitBid(10L, bidDto);

        assertNotNull(result);
        assertEquals(bidDto.getAmount(), result.getAmount());
        assertEquals(task, result.getTask());
        assertEquals(tasker, result.getTasker());
        verify(bidRepository, times(1)).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task is missing")
    void submitBid_TaskNotFound() {
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bidService.submitBid(10L, bidDto));
        verify(bidRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when task status is not OPEN")
    void submitBid_TaskNotOpen() {
        task.updateStatus(TaskStatus.CANCELLED);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bidService.submitBid(10L, bidDto));
        assertEquals("Bids can only be submitted for OPEN tasks.", ex.getMessage());
        verify(bidRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when tasker is missing")
    void submitBid_TaskerNotFound() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bidService.submitBid(10L, bidDto));
        verify(bidRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when requester tries to bid on own task")
    void submitBid_SelfBidding() {
        bidDto.setTaskerId(1L);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> bidService.submitBid(10L, bidDto));
        assertEquals("You cannot bid on your own task.", ex.getMessage());
        verify(bidRepository, never()).save(any());
    }
}