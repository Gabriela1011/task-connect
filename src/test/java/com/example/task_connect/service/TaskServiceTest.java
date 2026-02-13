package com.example.task_connect.service;

import com.example.task_connect.dto.TaskRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.*;
import com.example.task_connect.model.enums.TaskStatus;
import com.example.task_connect.repository.AddressRepository;
import com.example.task_connect.repository.CategoryRepository;
import com.example.task_connect.repository.TaskRepository;
import com.example.task_connect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @InjectMocks
    private TaskService taskService;

    private TaskRequestDTO taskDto;
    private User requester;
    private Category category;
    private Address address;

    @BeforeEach
    void setUp() {
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
}
