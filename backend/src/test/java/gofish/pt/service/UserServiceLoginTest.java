package gofish.pt.service;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import gofish.pt.dto.LoginRequestDTO;
import gofish.pt.dto.LoginResponseDTO;
import gofish.pt.entity.User;
import gofish.pt.exception.InvalidCredentialsException;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private LoginRequestDTO loginRequest;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        String hashedPassword = passwordEncoder.encode("password123");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword(hashedPassword);
        testUser.setLocation("Lisboa");
        testUser.setBalance(0.0);

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    @Requirement("GF-93")
    void login_withValidCredentials_shouldReturnToken() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1L, "john@example.com")).thenReturn("mock-jwt-token");

        LoginResponseDTO response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findByEmail("john@example.com");
        verify(jwtService).generateToken(1L, "john@example.com");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when email not found")
    @Requirement("GF-91")
    void login_withNonExistentEmail_shouldThrowException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        loginRequest.setEmail("unknown@example.com");

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail("unknown@example.com");
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    @Requirement("GF-91")
    void login_withWrongPassword_shouldThrowException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        loginRequest.setPassword("wrongpassword");

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail("john@example.com");
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should verify password against hashed value in database")
    @Requirement("GF-91")
    void login_shouldVerifyHashedPassword() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("token");

        userService.login(loginRequest);

        // Password was verified correctly (no exception thrown)
        verify(userRepository).findByEmail("john@example.com");
    }
}
