package com.example.task_connect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    @NotNull(message = "Rating is required")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    @Column(length = 1000)
    private String comments;


    // ---RELATIONSHIPS--

    @NotNull(message = "Task is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id", nullable = false)
    private Task task;

    @NotNull(message = "Reviewer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", referencedColumnName = "id", nullable = false)
    private User reviewer;

    @NotNull(message = "The person being reviewed is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_id", referencedColumnName = "id", nullable = false)
    private User reviewed;


    // ---GETTERS AND SETTERS---

    public Long getId() { return id; }

    public Integer getRating() { return rating; }
    public void  setRating(Integer rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Task getTask() { return task; }
    public void setTask(Task task) {
        this.task = task;
        if(task != null && !task.getReviews().contains(this)) {
            task.getReviews().add(this);
        }
    }

    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
        if(reviewer != null && !reviewer.getReviewsGiven().contains(this)) {
            reviewer.getReviewsGiven().add(this);
        }
    }

    public User getReviewed() { return reviewed; }
    public void setReviewed(User reviewed) {
        this.reviewed = reviewed;
        if(reviewed != null && !reviewed.getReviewsReceived().contains(this)) {
            reviewed.getReviewsReceived().add(this);
        }
    }
}
