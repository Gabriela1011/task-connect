package com.example.task_connect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    private Long id;

    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Lob
    private String bio;

    @Column(name = "tasker_rating", precision = 3, scale = 2)
    private BigDecimal taskerRating = BigDecimal.ZERO;

    @Column(name = "requester_rating", precision = 3, scale = 2)
    private BigDecimal requesterRating = BigDecimal.ZERO;


    // ---RELATIONSHIPS---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    @JsonIgnore
    private User user;


    // ---GETTERS AND SETTERS

    public Long getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public BigDecimal getTaskerRating() { return taskerRating; }
    public void updateTaskerRating(BigDecimal newAverage) {
        this.taskerRating = validateRating(newAverage);
    }

    public BigDecimal getRequesterRating() { return requesterRating; }
    public void updateRequesterRating(BigDecimal newAverage) {
        this.requesterRating = validateRating(newAverage);
    }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if(user != null && user.getProfile() != this){
            user.setProfile(this);
        }
    }


    //private helper method to avoid code duplication
    private BigDecimal validateRating(BigDecimal rating) {
        if(rating == null) return BigDecimal.ZERO;

        BigDecimal maxRating = BigDecimal.valueOf(5.0);
        BigDecimal minRating = BigDecimal.ZERO;

        //is newAverage > 5.0?
        if(rating.compareTo(maxRating) > 0) {
            return maxRating;
        } else if(rating.compareTo(minRating) < 0) {
            return minRating;
        } else {
            return rating;
        }
    }
}
