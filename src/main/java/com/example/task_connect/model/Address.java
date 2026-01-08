package com.example.task_connect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street cannot exceed 255 characters")
    @Column(length = 255)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(length = 100)
    private String city;

    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    @Column(name = "zip_code", length = 20)
    private String zipCode;


    // ---RELATIONSHIPS---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "address", fetch = FetchType.LAZY)
    private List<Task> tasksAtAddress = new ArrayList<>();


    // ---GETTERS AND SETTERS---

    public Long getId() { return id; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if(user != null && !user.getAddresses().contains(this)){
            user.getAddresses().add(this);
        }
    }

    public List<Task> getTasksAtAddress() { return tasksAtAddress; }
    public void setTasksAtAddress(List<Task> tasksAtAddress) { this.tasksAtAddress = tasksAtAddress; }

    public void addTask(Task task){
        this.tasksAtAddress.add(task);
        if(task.getAddress() != this) {
            task.setAddress(this);
        }
    }
}
