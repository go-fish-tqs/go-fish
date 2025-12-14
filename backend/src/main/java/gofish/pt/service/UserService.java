package gofish.pt.service;

import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
