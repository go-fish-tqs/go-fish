package gofish.pt.service;

import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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
}
