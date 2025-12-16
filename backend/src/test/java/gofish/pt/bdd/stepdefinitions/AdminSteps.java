package gofish.pt.bdd.stepdefinitions;

import gofish.pt.dto.AdminUserDTO;
import gofish.pt.entity.User;
import gofish.pt.entity.UserRole;
import gofish.pt.entity.UserStatus;
import gofish.pt.repository.UserRepository;
import gofish.pt.repository.UserRoleRepository;
import gofish.pt.repository.UserStatusRepository;
import gofish.pt.service.AdminService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminSteps {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    private Long adminUserId;
    private List<AdminUserDTO> userList;
    private Map<String, User> userMap = new HashMap<>();

    @Before
    public void setUp() {
        adminUserId = null;
        userList = null;
        userMap.clear();
    }

    @Given("an admin user {string} exists")
    public void an_admin_user_exists(String email) {
        User admin = userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setUsername("Admin");
            user.setEmail(email);
            user.setPassword("encodedpassword");
            user.setLocation("Admin Location");
            user.setBalance(0.0);
            return userRepository.save(user);
        });

        adminUserId = admin.getId();
        userMap.put(email, admin);

        // Ensure admin role exists
        if (userRoleRepository.findByUserId(adminUserId).isEmpty()) {
            userRoleRepository.save(new UserRole(adminUserId, UserRole.ROLE_ADMIN));
        }
        if (userStatusRepository.findByUserId(adminUserId).isEmpty()) {
            userStatusRepository.save(new UserStatus(adminUserId, UserStatus.STATUS_ACTIVE));
        }
    }

    @Given("a regular user {string} exists")
    public void a_regular_user_exists(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("User");
            newUser.setEmail(email);
            newUser.setPassword("encodedpassword");
            newUser.setLocation("Test Location");
            newUser.setBalance(0.0);
            User savedUser = userRepository.save(newUser);

            userRoleRepository.save(new UserRole(savedUser.getId(), UserRole.ROLE_USER));
            userStatusRepository.save(new UserStatus(savedUser.getId(), UserStatus.STATUS_ACTIVE));
            return savedUser;
        });
        userMap.put(email, user);
    }

    @Given("a suspended user {string} exists")
    public void a_suspended_user_exists(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("SuspendedUser");
            newUser.setEmail(email);
            newUser.setPassword("encodedpassword");
            newUser.setLocation("Test Location");
            newUser.setBalance(0.0);
            return userRepository.save(newUser);
        });

        Long actualId = user.getId();
        userMap.put(email, user);

        // Ensure role exists
        if (userRoleRepository.findByUserId(actualId).isEmpty()) {
            userRoleRepository.save(new UserRole(actualId, UserRole.ROLE_USER));
        }

        // Set status to suspended
        UserStatus status = userStatusRepository.findByUserId(actualId)
                .orElse(new UserStatus(actualId, UserStatus.STATUS_SUSPENDED));
        status.setStatus(UserStatus.STATUS_SUSPENDED);
        userStatusRepository.save(status);
    }

    @When("the admin suspends user {string}")
    public void the_admin_suspends_user(String email) {
        User user = userMap.get(email);
        adminService.suspendUser(user.getId(), adminUserId, "Test suspension");
    }

    @When("the admin unsuspends user {string}")
    public void the_admin_unsuspends_user(String email) {
        User user = userMap.get(email);
        adminService.reactivateUser(user.getId(), adminUserId);
    }

    @Then("user {string} should have status {string}")
    public void user_should_have_status(String email, String expectedStatus) {
        User user = userMap.get(email);
        String actualStatus = userStatusRepository.findByUserId(user.getId())
                .map(UserStatus::getStatus)
                .orElse(null);
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @When("the admin requests all users")
    public void the_admin_requests_all_users() {
        userList = adminService.getAllUsers();
    }

    @Then("the user list should contain user {string}")
    public void the_user_list_should_contain_user(String email) {
        assertThat(userList).isNotNull();
        assertThat(userList).anyMatch(u -> u.getEmail().equals(email));
    }
}
