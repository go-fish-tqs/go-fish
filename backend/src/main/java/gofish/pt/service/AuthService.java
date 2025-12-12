package gofish.pt.service;

import gofish.pt.dto.UserRegistrationRequest;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(UserRegistrationRequest req) {
        // email uniqueness
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setLocation(req.getLocation());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setBalance(0.0);

        return userRepository.save(user);
    }
}
