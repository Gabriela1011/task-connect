package com.example.task_connect.service;

import com.example.task_connect.exception.UserAlreadyExistsException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.Address;
import com.example.task_connect.model.Profile;
import com.example.task_connect.model.User;
import com.example.task_connect.repository.AddressRepository;
import com.example.task_connect.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should register a user with profile + multiple addresses")
    void registerUser_Success() {
        User user = new User();
        user.setEmail("test@example.ro");
        user.setPassword("test");

        Profile profile = new Profile();
        profile.setFirstName("Ionut");
        profile.setLastName("Ionescu");
        profile.setBio("Tehnician disponibil pentru task-uri electrice.");
        user.setProfile(profile);

        Address addr1 = new Address();
        addr1.setStreet("Calea Victoriei 100");
        addr1.setCity("București");

        Address addr2 = new Address();
        addr2.setStreet("Strada Primăverii 5");
        addr2.setCity("Cluj-Napoca");

        user.setAddresses(Arrays.asList(addr1, addr2));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(user);

        assertNotNull(savedUser.getProfile());
        assertEquals("Ionut", savedUser.getProfile().getFirstName());

        assertEquals(BigDecimal.ZERO, savedUser.getProfile().getTaskerRating());
        assertEquals(BigDecimal.ZERO, savedUser.getProfile().getRequesterRating());

        assertEquals(2, savedUser.getAddresses().size());
        assertEquals(savedUser, addr1.getUser());
        assertEquals(savedUser, addr2.getUser());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should register user successfully even if addresses are null (optional)")
    void registerUser_NoAddresses_Success() {
        User user = new User();
        user.setEmail("minimal@test.ro");
        user.setPassword("pass123");
        user.setAddresses(null);

        Profile profile = new Profile();
        profile.setFirstName("Maria");
        profile.setLastName("Popa");
        user.setProfile(profile);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(user);

        assertNotNull(savedUser.getProfile());
        assertEquals("Maria", savedUser.getProfile().getFirstName());

        assertEquals(BigDecimal.ZERO, savedUser.getProfile().getTaskerRating());
        assertEquals(BigDecimal.ZERO, savedUser.getProfile().getRequesterRating());

        assertNull(savedUser.getAddresses());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is taken")
    void registerUser_Failure_EmailExists() {
        User user = new User();
        user.setEmail("existent@example.ro");

        when(userRepository.findByEmail("existent@example.ro")).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if profile names are missing")
    void registerUser_MissingNames_ThrowsException() {
        User user = new User();
        user.setEmail("test@invalid.ro");
        Profile profile = new Profile();
        user.setProfile(profile);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(user)
        );

        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void findUserById_Success() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserById(10L);

        assertNotNull(foundUser);
        assertEquals(10L, foundUser.getId());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when ID does not exist")
    void findUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(99L));
    }

    @Test
    @DisplayName("Should link and save a new address to an existing user")
    void addAddressToUser_Success() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        Address newAddress = new Address();
        newAddress.setStreet("New Street");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(newAddress);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.addAddressToUser(1L, newAddress);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(user, newAddress.getUser());

        verify(addressRepository, times(1)).save(newAddress);
        verify(userRepository, times(1)).save(user);
    }
}
