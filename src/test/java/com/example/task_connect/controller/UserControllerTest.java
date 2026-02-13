package com.example.task_connect.controller;

import com.example.task_connect.exception.UserAlreadyExistsException;
import com.example.task_connect.exception.UserNotFoundException;
import com.example.task_connect.model.Address;
import com.example.task_connect.model.Profile;
import com.example.task_connect.model.User;
import com.example.task_connect.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Test
    @DisplayName("POST /api/users - Success (201 Created)")
    void registerUser_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.ro");
        user.setPassword("password");

        Profile profile = new Profile();
        profile.setFirstName("Ionut");
        profile.setLastName("Ionescu");
        user.setProfile(profile);

        when(userService.registerUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.ro"))
                .andExpect(jsonPath("$.profile.firstName").value("Ionut"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Success (200 OK)")
    void getUserById_Success() throws Exception {
        User user = new User();
        user.setEmail("find@test.ro");

        when(userService.findUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("find@test.ro"));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/addresses - Success (200 OK)")
    void addAddress_Success() throws Exception {
        Address addr = new Address();
        addr.setStreet("Strada Florilor 10");
        addr.setCity("Bucuresti");

        User user = new User();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("test@example.ro");
        user.setPassword("password");

        Profile profile = new Profile();
        profile.setFirstName("Ionut");
        profile.setLastName("Ionescu");
        user.setProfile(profile);

        when(userService.addAddressToUser(eq(1L), any(Address.class))).thenReturn(user);

        mockMvc.perform(post("/api/users/1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addr)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.ro"));


        org.mockito.ArgumentCaptor<Address> addressCaptor = org.mockito.ArgumentCaptor.forClass(Address.class);
        verify(userService).addAddressToUser(eq(1L), addressCaptor.capture());

        Address capturedAddress = addressCaptor.getValue();
        assertEquals("Strada Florilor 10", capturedAddress.getStreet());
        assertEquals("Bucuresti", capturedAddress.getCity());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Not Found (404)")
    void getUserById_NotFound() throws Exception {
        when(userService.findUserById(99L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/users - Email Conflict (409)")
    void registerUser_Conflict() throws Exception {
        User user = new User();
        user.setEmail("duplicate@test.ro");
        user.setPassword("password");

        Profile profile = new Profile();
        profile.setFirstName("Ionut");
        profile.setLastName("Ionescu");
        user.setProfile(profile);

        when(userService.registerUser(any(User.class)))
                .thenThrow(new UserAlreadyExistsException("Email already taken"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/users - Bad Request/Manual Validation (400)")
    void registerUser_BadRequest() throws Exception {
        User user = new User();
        user.setEmail("valid@email.ro");
        user.setPassword("parola123");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Names are mandatory"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Names are mandatory"));
    }
}
