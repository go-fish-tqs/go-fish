package gofish.pt.service;

import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setName("John Doe");
        registrationDTO.setEmail("john.doe@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setLocation("Lisboa");
    }

    @Test
    @DisplayName("Should register user with hashed password")
    void registerUser_withValidData_shouldHashPassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("John Doe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setPassword("$2a$10$hashedPassword");
        savedUser.setLocation("Lisboa");
        savedUser.setBalance(0.0);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(registrationDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("John Doe");
        assertThat(capturedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(capturedUser.getLocation()).isEqualTo("Lisboa");
        assertThat(capturedUser.getBalance()).isEqualTo(0.0);
        assertThat(capturedUser.getPassword()).isNotEqualTo("password123"); // Should be hashed
        assertThat(capturedUser.getPassword()).startsWith("$2a$"); // BCrypt prefix
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email exists")
    void registerUser_withExistingEmail_shouldThrowException() {
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationDTO))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already in use");

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should set balance to 0.0 for new user")
    void registerUser_shouldSetDefaultBalance() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setBalance(0.0);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.registerUser(registrationDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        assertThat(userCaptor.getValue().getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should check email existence before creating user")
    void registerUser_shouldCheckEmailFirst() {
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        userService.registerUser(registrationDTO);

        var inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByEmail("john.doe@example.com");
        inOrder.verify(userRepository).save(any(User.class));
    }
}
