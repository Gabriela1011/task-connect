package com.example.task_connect.model;

import com.example.task_connect.model.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    @Column(precision = 10, scale = 2, nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionStatus status =  TransactionStatus.PENDING;

    @Column(name = "t_timestamp", updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();


    // ---RELATIONSHIPS---

    @NotNull(message = "Task is required for a transaction")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id", unique = true, nullable = false)
    private Task task;


    // ---GETTERS AND SETTERS---

    public Long getId() { return id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionStatus getStatus() { return status; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public Task getTask() { return task; }
    public void setTask(Task task) {
        this.task = task;
        if(task != null && task.getTransaction() != this) {
            task.setTransaction(this);
        }
    }


    // ---METHODS---

    //change transaction status
    public void updateStatus(TransactionStatus newStatus) {
        if(newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        switch(this.status) {
            case PENDING:
                //from PENDING, a transaction can only move to SUCCESS or FAILED
                if(newStatus != TransactionStatus.SUCCESS && newStatus != TransactionStatus.FAILED) {
                    throw new IllegalStateException("Pending transactions can only succeed or fail.");
                }
                break;

            case SUCCESS:
                //only a successful transaction can be REFUNDED
                if(newStatus != TransactionStatus.REFUNDED){
                    throw new IllegalStateException("Only successful transactions can be refunded.");
                }
                break;

            //these are terminal state
            //you cannot re-process a failed or refunded payment
            case FAILED:
            case REFUNDED:
                throw new IllegalStateException("Cannot change status of a " + this.status + " transaction.");

            default:
                throw new IllegalStateException("Unknown transaction status: " + this.status);
        }

        this.status = newStatus;
    }
}
