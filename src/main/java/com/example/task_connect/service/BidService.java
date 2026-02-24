package com.example.task_connect.service;

import com.example.task_connect.dto.BidRequestDTO;
import com.example.task_connect.exception.ResourceNotFoundException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.*;
import com.example.task_connect.model.enums.TaskStatus;
import com.example.task_connect.repository.BidRepository;
import com.example.task_connect.repository.TaskRepository;
import com.example.task_connect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public BidService(BidRepository bidRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.bidRepository = bidRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Bid submitBid(Long taskId, BidRequestDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new IllegalStateException("Bids can only be submitted for OPEN tasks.");
        }

        User tasker = userRepository.findById(dto.getTaskerId())
                .orElseThrow(() -> new UserNotFoundException("Tasker not found with ID: " + dto.getTaskerId()));

        if (task.getRequester().getId().equals(tasker.getId())) {
            throw new IllegalStateException("You cannot bid on your own task.");
        }


        Bid bid = new Bid();
        bid.setAmount(dto.getAmount());
        bid.setMessage(dto.getMessage());

        bid.setTask(task);
        bid.setTasker(tasker);

        return bidRepository.save(bid);
    }
}