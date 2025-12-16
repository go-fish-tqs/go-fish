package gofish.pt.service;

import gofish.pt.dto.UserResponseDTO;
import gofish.pt.dto.UserUpdateDTO;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUpdateTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setLocation("Porto");
        user.setPhone("+351912345678");
        user.setAddress("Rua Test 123");
        user.setProfilePhoto("https://example.com/photo.jpg");
        user.setBalance(100.0);

        updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPhone("+351987654321");
        updateDTO.setAddress("Rua Updated 456");
        updateDTO.setProfilePhoto("https://example.com/new-photo.jpg");
        updateDTO.setLocation("Lisboa");
    }

    @Test
    void updateUser_WhenAllFieldsProvided_ShouldUpdateSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.updateUser(1L, 1L, updateDTO);

        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPhone()).isEqualTo("+351987654321");
        assertThat(result.getAddress()).isEqualTo("Rua Updated 456");
        assertThat(result.getProfilePhoto()).isEqualTo("https://example.com/new-photo.jpg");
        assertThat(result.getLocation()).isEqualTo("Lisboa");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, 1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_WhenUserIdMismatch_ShouldThrowException() {
        assertThatThrownBy(() -> userService.updateUser(1L, 2L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only update your own profile");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenEmailAlreadyExists_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, 1L, updateDTO))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenEmailUnchanged_ShouldNotCheckUniqueness() {
        updateDTO.setEmail("test@example.com"); // Same as current
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUser(1L, 1L, updateDTO);

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenPartialUpdate_ShouldOnlyUpdateProvidedFields() {
        UserUpdateDTO partialDTO = new UserUpdateDTO();
        partialDTO.setUsername("newusername");
        partialDTO.setPhone("+351999999999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.updateUser(1L, 1L, partialDTO);

        assertThat(result.getUsername()).isEqualTo("newusername");
        assertThat(result.getPhone()).isEqualTo("+351999999999");
        assertThat(result.getEmail()).isEqualTo("test@example.com"); // Unchanged
        assertThat(result.getAddress()).isEqualTo("Rua Test 123"); // Unchanged

        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserProfile_WhenUserExists_ShouldReturnUserResponseDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getUserProfile(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPhone()).isEqualTo("+351912345678");
        assertThat(result.getAddress()).isEqualTo("Rua Test 123");
        assertThat(result.getProfilePhoto()).isEqualTo("https://example.com/photo.jpg");
        assertThat(result.getLocation()).isEqualTo("Porto");
        assertThat(result.getBalance()).isEqualTo(100.0);
    }

    @Test
    void getUserProfile_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
