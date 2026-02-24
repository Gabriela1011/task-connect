package com.example.task_connect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Unique email address for the user")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt =  LocalDateTime.now();


    // ---RELATIONSHIPS---

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    //@JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Valid
    private List<Address> addresses = new ArrayList<>();

    //Tasks this user created
    @JsonIgnore
    @OneToMany(mappedBy = "requester",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> requestedTasks = new ArrayList<>();

    //Tasks this user is assigned to do
    @JsonIgnore
    @OneToMany(mappedBy = "tasker", fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tasker",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "reviewer", fetch =  FetchType.LAZY)
    private List<Review> reviewsGiven = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "reviewed", fetch = FetchType.LAZY)
    private List<Review> reviewsReceived = new ArrayList<>();


    // ---GETTERS AND SETTERS---

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) {
        this.profile = profile;
        if(profile != null && profile.getUser() != this) {
            profile.setUser(this);
        }
    }


    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    //helper method to add an address
    //this should be used in SERVICE
    public void addAddress(Address address) {
        this.addresses.add(address);
        if(address.getUser() != this) {
            address.setUser(this);
        }
    }

    public List<Task> getRequestedTasks() { return requestedTasks; }
    //public void setRequestedTasks(List<Task> requestedTasks) { this.requestedTasks = requestedTasks; }

    public void addRequestedTask(Task task) {
        this.requestedTasks.add(task);
        if(task.getRequester() != this) {
            task.setRequester(this);
        }
    }


    public List<Task> getAssignedTasks() { return assignedTasks; }
    //public void setAssignedTasks(List<Task> assignedTasks) { this.assignedTasks = assignedTasks; }

    public void addAssignedTask(Task task) {
        this.assignedTasks.add(task);
        if(task.getTasker() != this) {
            task.setTasker(this);
        }
    }

    public List<Bid> getBids() { return bids; }
    //public void setBids(List<Bid> bids) { this.bids = bids; }

    public void addBid(Bid bid) {
        this.bids.add(bid);
        if(bid.getTasker() != this) {
            bid.setTasker(this);
        }
    }

    public List<Review> getReviewsGiven() { return reviewsGiven; }
    //public void setReviewsGiven(List<Review> reviewsGiven) { this.reviewsGiven = reviewsGiven; }

    public void addReviewGiven(Review review){
        this.reviewsGiven.add(review);
        if(review.getReviewer() != this) {
            review.setReviewer(this);
        }
    }

    public  List<Review> getReviewsReceived() { return reviewsReceived; }
    //public void setReviewsReceived(List<Review> reviewsReceived) { this.reviewsReceived = reviewsReceived; }

    public void addReviewReceived(Review review){
        this.reviewsReceived.add(review);
        if(review.getReviewed() != this) {
            review.setReviewed(this);
        }
    }
}
