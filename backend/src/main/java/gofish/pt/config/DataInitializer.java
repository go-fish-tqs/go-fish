package gofish.pt.config;

import gofish.pt.entity.User;
import gofish.pt.entity.UserRole;
import gofish.pt.entity.UserStatus;
import gofish.pt.repository.UserRepository;
import gofish.pt.repository.UserRoleRepository;
import gofish.pt.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default data at application startup.
 * Creates admin user if it doesn't exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserStatusRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@gofish.pt";
    @org.springframework.beans.factory.annotation.Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    private static final String ADMIN_USERNAME = "admin";

    @Override
    public void run(String... args) {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        log.info("Creating admin user...");

        // Create admin user with encoded password
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setLocation("Admin HQ");
        admin.setBalance(0.0);

        User savedAdmin = userRepository.save(admin);
        log.info("Admin user created with ID: {}", savedAdmin.getId());

        // Create admin role
        UserRole adminRole = new UserRole();
        adminRole.setUserId(savedAdmin.getId());
        adminRole.setRole(UserRole.ROLE_ADMIN);
        userRoleRepository.save(adminRole);
        log.info("Admin role assigned");

        // Create active status
        UserStatus adminStatus = new UserStatus();
        adminStatus.setUserId(savedAdmin.getId());
        adminStatus.setStatus(UserStatus.STATUS_ACTIVE);
        userStatusRepository.save(adminStatus);
        log.info("Admin status set to ACTIVE");

        log.info("Admin user setup complete! Email: {} Password: {}", ADMIN_EMAIL, "******");
    }
}
