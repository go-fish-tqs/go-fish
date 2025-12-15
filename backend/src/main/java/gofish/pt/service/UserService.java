package gofish.pt.service;

import gofish.pt.dto.LoginRequestDTO;
import gofish.pt.dto.LoginResponseDTO;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.entity.UserRole;
import gofish.pt.entity.UserStatus;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.exception.InvalidCredentialsException;
import gofish.pt.repository.UserRepository;
import gofish.pt.repository.UserRoleRepository;
import gofish.pt.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.exception.InvalidCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserStatusRepository userStatusRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

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

        User savedUser = userRepository.save(user);

        // Create default role (USER)
        UserRole role = new UserRole(savedUser.getId(), UserRole.ROLE_USER);
        userRoleRepository.save(role);

        // Create default status (ACTIVE)
        UserStatus status = new UserStatus(savedUser.getId(), UserStatus.STATUS_ACTIVE);
        userStatusRepository.save(status);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Check if user is not deleted
        UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                .orElse(null);
        if (userStatus != null && UserStatus.STATUS_DELETED.equals(userStatus.getStatus())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Get user role
        String role = userRoleRepository.findByUserId(user.getId())
                .map(UserRole::getRole)
                .orElse(UserRole.ROLE_USER);

        // Get user status for response (we already fetched it above but let's be clean)
        String status = userStatus != null ? userStatus.getStatus() : UserStatus.STATUS_ACTIVE;

        // Generate token with role
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role);

        // Return response with token, user info, role, and status
        return new LoginResponseDTO(
                user.getId(),
                token,
                user.getUsername(),
                user.getEmail(),
                role,
                status);
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

    /**
     * Check if a user is active (not suspended or deleted)
     */
    public boolean isUserActive(Long userId) {
        return userStatusRepository.findByUserId(userId)
                .map(status -> UserStatus.STATUS_ACTIVE.equals(status.getStatus()))
                .orElse(true); // Default to active if no status record exists
    }

    /**
     * Get user role
     */
    public String getUserRole(Long userId) {
        return userRoleRepository.findByUserId(userId)
                .map(UserRole::getRole)
                .orElse(UserRole.ROLE_USER);
    }
}
