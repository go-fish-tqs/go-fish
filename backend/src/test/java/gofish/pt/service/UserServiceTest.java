package gofish.pt.service;

import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.repository.UserRepository;
import gofish.pt.repository.UserRoleRepository;
import gofish.pt.repository.UserStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    @Requirement("GF-91")
    void registerUser_withValidData_shouldHashPassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("John Doe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setPassword("$2a$10$hashedPassword");
        savedUser.setLocation("Lisboa");
        savedUser.setBalance(0.0);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRoleRepository.save(any())).thenReturn(null);
        when(userStatusRepository.save(any())).thenReturn(null);

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
    @Requirement("GF-91")
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
    @Requirement("GF-91")
    void registerUser_shouldSetDefaultBalance() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setBalance(0.0);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRoleRepository.save(any())).thenReturn(null);
        when(userStatusRepository.save(any())).thenReturn(null);

        userService.registerUser(registrationDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should check email existence before creating user")
    @Requirement("GF-91")
    void registerUser_shouldCheckEmailFirst() {
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(userRoleRepository.save(any())).thenReturn(null);
        when(userStatusRepository.save(any())).thenReturn(null);

        userService.registerUser(registrationDTO);

        var inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByEmail("john.doe@example.com");
        inOrder.verify(userRepository).save(any(User.class));
    }

    @Test
    @Requirement("GF-66")
    void getUserBookings_returnsBookings() {
        Long userId = 1L;
        User user = mock(User.class);
        Booking booking = mock(Booking.class);
        List<Booking> bookings = List.of(booking);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getBookings()).thenReturn(bookings);

        List<Booking> result = userService.getUserBookings(userId);

        assertSame(bookings, result);
        verify(userRepository).findById(userId);
        verify(user).getBookings();
    }

    @Test
    @Requirement("GF-91")
    void getUserBookings_userNotFound_throws() {
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserBookings(userId));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository).findById(userId);
    }

    @Test
    @Requirement("GF-91")
    void getUserOwnedBookings_returnsOwnedBookings() {
        Long userId = 3L;
        User user = mock(User.class);
        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);
        Booking b1 = mock(Booking.class);
        Booking b2 = mock(Booking.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getItems()).thenReturn(List.of(item1, item2));
        when(item1.getBookings()).thenReturn(List.of(b1));
        when(item2.getBookings()).thenReturn(List.of(b2));

        List<Booking> result = userService.getUserOwnedBookings(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(b1));
        assertTrue(result.contains(b2));
        verify(userRepository).findById(userId);
        verify(user).getItems();
        verify(item1).getBookings();
        verify(item2).getBookings();
    }

    @Test
    @Requirement("GF-91")
    void getUserOwnedItems_returnsItems() {
        Long userId = 4L;
        User user = mock(User.class);
        Item item = mock(Item.class);
        List<Item> items = List.of(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getItems()).thenReturn(items);

        List<Item> result = userService.getUserOwnedItems(userId);

        assertSame(items, result);
        verify(userRepository).findById(userId);
        verify(user).getItems();
    }

    @Test
    @Requirement("GF-91")
    void getUserOwnedItems_userNotFound_throws() {
        Long userId = 5L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserOwnedItems(userId));
        verify(userRepository).findById(userId);
    }
}
