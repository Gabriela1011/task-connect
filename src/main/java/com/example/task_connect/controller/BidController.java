package com.example.task_connect.controller;

import com.example.task_connect.dto.BidRequestDTO;
import com.example.task_connect.model.Bid;
import com.example.task_connect.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/{taskId}/bids")
@Tag(name = "Bidding System", description = "Endpoint for taskers to submit bids on specific tasks")
public class BidController {

    private final BidService bidService;

    @Autowired
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }


    @Operation(
            summary = "Submit a new bid",
            description = "Allows a tasker to submit a financial offer and a message for an OPEN task. " +
                    "The Tasker cannot be the same person as the Task Requester."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bid successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation failed, task is not OPEN, or Tasker is the Requester"),
            @ApiResponse(responseCode = "404", description = "Task or Tasker ID not found")
    })
    @PostMapping
    public ResponseEntity<Bid> submitBid(
            @Parameter(description = "ID of the task to bid on")
            @PathVariable Long taskId,
            @Valid @RequestBody BidRequestDTO bidRequest) {

        Bid createdBid = bidService.submitBid(taskId, bidRequest);
        return new ResponseEntity<>(createdBid, HttpStatus.CREATED);
    }
}