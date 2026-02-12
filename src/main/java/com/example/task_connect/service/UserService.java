package com.example.task_connect.service;

import com.example.task_connect.exception.UserAlreadyExistsException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.Address;
import com.example.task_connect.model.Profile;
import com.example.task_connect.model.User;
import com.example.task_connect.repository.AddressRepository;
import com.example.task_connect.repository.ProfileRepository;
import com.example.task_connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       ProfileRepository profileRepository,
                       AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public User registerUser(User user) {
        //unique email
        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException("The email " + user.getEmail() + " is already registered.");
        });

        //initialize profile
        Profile profile = (user.getProfile() != null)
                ? user.getProfile()
                : new Profile();

        user.setProfile(profile);
        profile.updateTaskerRating(BigDecimal.ZERO);
        profile.updateRequesterRating(BigDecimal.ZERO);

        //link addresses to user
        if(user.getAddresses() != null) {
            user.getAddresses().forEach(a -> a.setUser(user));
        }

        return userRepository.save(user);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public User addAddressToUser(Long userId, Address newAddress) {
        User user = findUserById(userId);
        user.addAddress(newAddress);

        addressRepository.save(newAddress);
        return userRepository.save(user);
    }
}
