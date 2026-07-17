package com.example.secondhand.controller;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.InvalidCredentialsException;
import com.example.secondhand.exception.UserAlreadyExistsException;
import com.example.secondhand.model.Role;
import com.example.secondhand.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== register ====================

    @Test
    void register_shouldReturn201_whenDataIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ali Ahmadi");
        request.setUsername("ali123");
        request.setPassword("123456");
        request.setPhone("09121234567");
        request.setEmail("ali@example.com");

        when(userService.register(any(RegisterRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageCode").value("REGISTER_SUCCESS"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void register_shouldReturn400_whenTitleFieldsAreBlank() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setUsername("");
        request.setPassword("123456");
        request.setPhone("");
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenEmailFormatIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ali Ahmadi");
        request.setUsername("ali123");
        request.setPassword("123456");
        request.setPhone("09121234567");
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409_whenUsernameAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ali Ahmadi");
        request.setUsername("ali123");
        request.setPassword("123456");
        request.setPhone("09121234567");
        request.setEmail("ali@example.com");

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("نام کاربری تکراری است"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("نام کاربری تکراری است"));
    }

    // ==================== login ====================

    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("123456");

        LoginResponse loginResponse = new LoginResponse("jwt-token", 1L, "ali123", Role.USER);

        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("ali123"));
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("wrong-password");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("نام کاربری یا رمز عبور نادرست است"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("نام کاربری یا رمز عبور نادرست است"));
    }

    @Test
    void login_shouldReturn400_whenUsernameIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
