package com.example.task_connect.model;

import com.example.task_connect.model.enums.BidStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(precision = 10, scale = 2, nullable = false, updatable = false)
    private BigDecimal amount;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BidStatus status =  BidStatus.PENDING;


    // ---RELATIONSHIPS---

    @NotNull(message = "Task is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"bids", "requester", "address", "category", "reviews"})
    private Task task;

    @NotNull(message = "Bidder (Tasker) is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasker_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"requestedTasks", "assignedTasks", "bids", "password", "addresses"})
    private User tasker;


    // ---GETTERS AND SETTERS---

    public Long getId() { return id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public  BidStatus getStatus() { return status; }

    public Task getTask() { return task; }
    public void setTask(Task task) {
        this.task = task;
        if(task != null && !task.getBids().contains(this)) {
            task.getBids().add(this);
        }
    }

    public User getTasker() { return tasker; }
    public void setTasker(User tasker) {
        this.tasker = tasker;
        if(tasker != null && !tasker.getBids().contains(this)) {
            tasker.getBids().add(this);
        }
    }


    // ---METHODS---

    //change bid status
    public void updateStatus(BidStatus newStatus) {
        if(newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        switch(this.status) {
            case PENDING:
                //from PENDING, a bid can move to any other state
                //(accepted by requester, rejected by requester, or cancelled by tasker)
                break;

            case ACCEPTED:
                //once a bid is accepted, it's a contract
                //it cannot be changed to REJECTED or CANCELLED without a separate
                //TASK CANCELLATION process
                if(newStatus != BidStatus.PENDING) {
                    //only allow going back if specifically needed
                    throw new IllegalStateException("An ACCEPTED bid is a final agreement.");
                }
                break;

            case REJECTED:
            case CANCELLED:
                //terminal states
                throw new IllegalStateException("Cannot change status of a " + this.status + " bid.");

            default:
                throw new IllegalStateException("Unknown bid status: " + this.status);
        }

        this.status = newStatus;
    }

}
