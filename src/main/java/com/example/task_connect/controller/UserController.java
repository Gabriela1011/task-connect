package com.example.task_connect.controller;

import com.example.task_connect.model.Address;
import com.example.task_connect.model.User;
import com.example.task_connect.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user registration, address management, and profile retrieval")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user with profile and optional addresses",
            description = "Creates a new user account, automatically initializes the profile with 0.0 ratings, " +
                    "and allows for the simultaneous addition of one or more physical addresses."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input (validation failed)"),
            @ApiResponse(responseCode = "409", description = "Conflict - The email address is already in use")
    })
    @PostMapping
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED); //201
    }


    @Operation(
            summary = "Add a new address to an existing user",
            description = "Links a new physical address to a user identified by the userId provided in the URL path."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address successfully added and user profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input (validation failed)"),
            @ApiResponse(responseCode = "404", description = "User not found with the provided ID")
    })
    @PostMapping("/{userId}/addresses")
    public ResponseEntity<User> addAddress(@PathVariable Long userId, @Valid @RequestBody Address address) {
        User updatedUser = userService.addAddressToUser(userId, address);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }


    @Operation(
            summary = "Retrieve a specific user by ID",
            description = "Fetches the full details of a user, including their profile information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and details retrieved"),
            @ApiResponse(responseCode = "404", description = "No user exists with the specified ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
