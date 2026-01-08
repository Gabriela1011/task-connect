package com.example.task_connect.model.enums;

public enum BidStatus {
    PENDING,  //Initial state when a tasker makes an offer
    ACCEPTED, //when the requester chooses this bid
    REJECTED, //when the requester declines this bid
    CANCELLED //if the tasker withdraws the bid
}
