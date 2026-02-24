package com.example.task_connect.service;

import com.example.task_connect.dto.TaskRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.*;
import com.example.task_connect.model.enums.BidStatus;
import com.example.task_connect.model.enums.TaskStatus;
import com.example.task_connect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private TaskService taskService;

    private TaskRequestDTO taskDto;
    private User requester;
    private Category category;
    private Address address;

    //acceptBid
    private Task existingTask;
    private Bid winningBid;
    private Bid otherBid;
    private User tasker;

    @BeforeEach
    void setUp() {
        //Setup createTask
        taskDto = new TaskRequestDTO();
        taskDto.setTitle("Test Task");
        taskDto.setBudget(new BigDecimal("100.00"));
        taskDto.setRequesterId(1L);
        taskDto.setCategoryId(1L);
        taskDto.setAddressId(1L);

        requester = new User();
        requester.setEmail("test@example.ro");
        Profile profile = new Profile();
        profile.setFirstName("Ion");
        profile.setLastName("Popescu");

        requester.setProfile(profile);
        category = new Category();
        address = new Address();

        //Setup acceptBid
        tasker = new User();
        ReflectionTestUtils.setField(tasker, "id", 2L);

        existingTask = new Task();
        ReflectionTestUtils.setField(existingTask, "id", 10L);
        existingTask.setRequester(requester);

        winningBid = new Bid();
        ReflectionTestUtils.setField(winningBid, "id", 5L);
        winningBid.setTask(existingTask);
        winningBid.setTasker(tasker);

        otherBid = new Bid();
        ReflectionTestUtils.setField(otherBid, "id", 6L);
        otherBid.setTask(existingTask);

        List<Bid> bids = new ArrayList<>();
        bids.add(winningBid);
        bids.add(otherBid);
        ReflectionTestUtils.setField(existingTask, "bids", bids);
    }

    @Test
    @DisplayName("Should successfully create a task when all IDs are valid")
    void createTask_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task createdTask = taskService.createTask(taskDto);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals(TaskStatus.OPEN, createdTask.getStatus());
        assertEquals(requester, createdTask.getRequester());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when requester does not exist")
    void createTask_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> taskService.createTask(taskDto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist")
    void createTask_CategoryNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(taskDto));

        assertTrue(ex.getMessage().contains("Category not found"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when address does not exist")
    void createTask_AddressNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(taskDto));

        assertTrue(ex.getMessage().contains("Address not found"));
        verify(taskRepository, never()).save(any());
    }


    //acceptBid tests

    @Test
    @DisplayName("Should successfully accept a bid and assign the task")
    void acceptBid_Success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));
        when(bidRepository.findById(5L)).thenReturn(Optional.of(winningBid));

        taskService.acceptBid(10L, 5L);

        assertEquals(TaskStatus.ASSIGNED, existingTask.getStatus());
        assertEquals(BidStatus.ACCEPTED, winningBid.getStatus());
        assertEquals(BidStatus.REJECTED, otherBid.getStatus());
        assertEquals(tasker, existingTask.getTasker());

        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to accept bid for a non-OPEN task")
    void acceptBid_TaskNotOpen() {
        existingTask.updateStatus(TaskStatus.CANCELLED);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> taskService.acceptBid(10L, 5L));

        assertTrue(ex.getMessage().contains("Only OPEN tasks can accept bids"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when bid does not belong to the task")
    void acceptBid_BidMismatch() {
        Task unrelatedTask = new Task();
        ReflectionTestUtils.setField(unrelatedTask, "id", 99L);
        winningBid.setTask(unrelatedTask);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));
        when(bidRepository.findById(5L)).thenReturn(Optional.of(winningBid));

        assertThrows(IllegalArgumentException.class, () -> taskService.acceptBid(10L, 5L));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when bid ID is invalid")
    void acceptBid_BidNotFound() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));
        when(bidRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.acceptBid(10L, 5L));
    }

}
