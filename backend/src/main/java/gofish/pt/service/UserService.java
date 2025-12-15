package gofish.pt.service;

import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import gofish.pt.dto.LoginRequestDTO;
import gofish.pt.dto.LoginResponseDTO;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.dto.UserResponseDTO;
import gofish.pt.dto.UserUpdateDTO;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.exception.InvalidCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }

        // Create new user
        User user = new User();
        user.setUsername(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setLocation(registrationDTO.getLocation());
        user.setBalance(0.0);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Generate token
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        // Return response with token and user info
        return new LoginResponseDTO(
                user.getId(),
                token,
                user.getUsername(),
                user.getEmail()
        );
    }
    public List<Booking> getUserBookings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return user.getBookings();
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserOwnedBookings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return user.getItems().stream()
                .flatMap(item -> item.getBookings().stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Item> getUserOwnedItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return user.getItems();
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, Long authenticatedUserId, UserUpdateDTO updateDTO) {
        // Verify that the user can only update their own profile
        if (!userId.equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only update your own profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Check email uniqueness if email is being changed
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new DuplicateEmailException("Email already in use");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // Update fields if provided
        if (updateDTO.getUsername() != null) {
            user.setUsername(updateDTO.getUsername());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getProfilePhoto() != null) {
            user.setProfilePhoto(updateDTO.getProfilePhoto());
        }
        if (updateDTO.getLocation() != null) {
            user.setLocation(updateDTO.getLocation());
        }

        User updatedUser = userRepository.save(user);

        return new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getAddress(),
                updatedUser.getProfilePhoto(),
                updatedUser.getLocation(),
                updatedUser.getBalance()
        );
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getProfilePhoto(),
                user.getLocation(),
                user.getBalance()
        );
    }
}
