package com.example.task_connect.controller;

import com.example.task_connect.model.Address;
import com.example.task_connect.model.User;
import com.example.task_connect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //register a new user with profile
    @PostMapping
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED); //201
    }

    //add a new address to existing user
    @PostMapping("/{userId}/addresses")
    public ResponseEntity<User> addAddress(@PathVariable Long userId, @Valid @RequestBody Address address) {
        User updatedUser = userService.addAddressToUser(userId, address);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    //retrieve a specific user
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
