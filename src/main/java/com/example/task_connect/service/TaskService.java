package com.example.task_connect.service;

import com.example.task_connect.dto.TaskRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.Address;
import com.example.task_connect.model.Category;
import com.example.task_connect.model.Task;
import com.example.task_connect.model.User;
import com.example.task_connect.repository.AddressRepository;
import com.example.task_connect.repository.CategoryRepository;
import com.example.task_connect.repository.TaskRepository;
import com.example.task_connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AddressRepository addressRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       CategoryRepository categoryRepository, AddressRepository addressRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Task createTask(TaskRequestDTO dto) {
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new UserNotFoundException("Requester not found with ID: " + dto.getRequesterId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        Address address = addressRepository.findById(dto.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + dto.getAddressId()));

        //create the real entity and move the data from the DTO
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setBudget(dto.getBudget());

        task.setRequester(requester);
        task.setCategory(category);
        task.setAddress(address);

        return taskRepository.save(task);
    }
}
