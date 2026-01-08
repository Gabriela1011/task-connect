package com.example.task_connect.model;

import com.example.task_connect.model.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    private String description;

    @DecimalMin(value = "0.0", message = "Budget must be a positive number")
    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.OPEN;


    // ---RELATIONSHIPS---

    @NotNull(message = "requester_id is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasker_id", referencedColumnName = "id")
    private User tasker;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @NotNull(message = "Address is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @OneToOne(mappedBy = "task")
    private Transaction transaction;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();


    // ---GETTERS AND SETTERS---

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public TaskStatus getStatus() { return status; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) {
        this.requester = requester;
        if(requester != null && !requester.getRequestedTasks().contains(this)) {
            requester.addRequestedTask(this);
        }
    }

    public User getTasker() { return tasker; }
    public void setTasker(User tasker) {
        this.tasker = tasker;
        if(tasker != null && !tasker.getAssignedTasks().contains(this)) {
            tasker.addAssignedTask(this);
        }
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) {
        this.category = category;
        if(category != null && !category.getTasks().contains(this)) {
            category.addTask(this);
        }
    }

    public Address getAddress() { return address; }
    public void setAddress(Address address) {
        this.address = address;
        if(address != null && !address.getTasksAtAddress().contains(this)){
            address.addTask(this);
        }
    }

    public List<Bid> getBids() { return bids; }
    public void setBids(List<Bid> bids) { this.bids = bids; }

    public void addBid(Bid bid) {
        this.bids.add(bid);
        if(bid.getTask() != this) {
            bid.setTask(this);
        }
    }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if(transaction != null && transaction.getTask() != this) {
            transaction.setTask(this);
        }
    }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public void addReview(Review review) {
        this.reviews.add(review);
        if(review.getTask() != this) {
            review.setTask(this);
        }
    }


    // ---METHODS---

    //change task status
    public void updateStatus(TaskStatus newStatus) {
        if(newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        //define allowed transitions
        switch(this.status) {
            case OPEN:
                //an OPEN task can be ASSIGNED to a worker or CANCELLED by the requester
                if(newStatus != TaskStatus.ASSIGNED && newStatus != TaskStatus.CANCELLED) {
                    throw new IllegalStateException("Open tasks can only be assigned or cancelled");
                }
                break;

            case ASSIGNED:
                //an ASSIGNED task can be COMPLETED after work or CANCELLED if the deal falls through
                if(newStatus != TaskStatus.COMPLETED && newStatus != TaskStatus.CANCELLED) {
                    throw new IllegalStateException("Assigned tasks can only be completed or cancelled");
                }
                break;

            //terminal states: once a task is done or cancelled, it cannot change again
            case COMPLETED:
            case CANCELLED:
                throw new IllegalStateException("Cannot change status of a " + this.status + " task");

            default:
                throw new IllegalStateException("Unknown task status " + this.status);
        }

        this.status = newStatus;
    }

    public void cancel() {
        //reuse the logic: can't cancel if already completed
        this.updateStatus(TaskStatus.CANCELLED);
    }

}
