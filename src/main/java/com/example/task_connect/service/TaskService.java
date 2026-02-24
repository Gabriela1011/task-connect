package com.example.task_connect.service;

import com.example.task_connect.dto.TaskRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.*;
import com.example.task_connect.model.enums.BidStatus;
import com.example.task_connect.model.enums.TaskStatus;
import com.example.task_connect.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AddressRepository addressRepository;
    private final BidRepository bidRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       CategoryRepository categoryRepository, AddressRepository addressRepository, BidRepository bidRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.addressRepository = addressRepository;
        this.bidRepository = bidRepository;
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

    @Transactional
    public void acceptBid(Long taskId, Long bidId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new IllegalStateException("Task is " + task.getStatus() + ". Only OPEN tasks can accept bids.");
        }

        Bid winningBid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with ID: " + bidId));

        if (!winningBid.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("This bid does not belong to the specified task.");
        }

        winningBid.updateStatus(BidStatus.ACCEPTED);
        task.updateStatus(TaskStatus.ASSIGNED);

        task.setTasker(winningBid.getTasker());
        task.getBids().stream()
                .filter(bid -> !bid.getId().equals(bidId) && bid.getStatus() == BidStatus.PENDING)
                .forEach(bid -> bid.updateStatus(BidStatus.REJECTED));

        taskRepository.save(task);
    }
}
