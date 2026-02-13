package com.example.task_connect.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @DecimalMin(value = "0.0", message = "Budget must be a positive number")
    private BigDecimal budget;

    @NotNull(message = "requester_id is required")
    private Long requesterId;

    @NotNull(message = "category_id is required")
    private Long categoryId;

    @NotNull(message = "address_id is required")
    private Long addressId;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
}
