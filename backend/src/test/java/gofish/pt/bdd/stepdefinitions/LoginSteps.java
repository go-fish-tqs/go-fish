package gofish.pt.bdd.stepdefinitions;

import gofish.pt.dto.LoginRequestDTO;
import gofish.pt.dto.LoginResponseDTO;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.User;
import gofish.pt.entity.UserRole;
import gofish.pt.entity.UserStatus;
import gofish.pt.exception.InvalidCredentialsException;
import gofish.pt.repository.UserRepository;
import gofish.pt.repository.UserRoleRepository;
import gofish.pt.repository.UserStatusRepository;
import gofish.pt.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private LoginResponseDTO loginResponse;
    private Exception loginException;

    @Before
    public void setUp() {
        loginResponse = null;
        loginException = null;
    }

    @Given("a user exists with email {string} and password {string}")
    public void a_user_exists_with_email_and_password(String email, String password) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setUsername("Test User");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setLocation("Test Location");
            user.setBalance(0.0);
            User savedUser = userRepository.save(user);

            // Create role and status
            userRoleRepository.save(new UserRole(savedUser.getId(), UserRole.ROLE_USER));
            userStatusRepository.save(new UserStatus(savedUser.getId(), UserStatus.STATUS_ACTIVE));
        }
    }

    @When("I attempt to login with email {string} and password {string}")
    public void i_attempt_to_login_with_email_and_password(String email, String password) {
        LoginRequestDTO request = new LoginRequestDTO(email, password);
        try {
            loginResponse = userService.login(request);
        } catch (InvalidCredentialsException e) {
            loginException = e;
        }
    }

    @Then("the login should be successful")
    public void the_login_should_be_successful() {
        assertThat(loginException).isNull();
        assertThat(loginResponse).isNotNull();
    }

    @Then("I should receive a valid JWT token")
    public void i_should_receive_a_valid_jwt_token() {
        assertThat(loginResponse.getToken()).isNotNull();
        assertThat(loginResponse.getToken()).isNotEmpty();
    }

    @Then("the response should contain user details")
    public void the_response_should_contain_user_details() {
        assertThat(loginResponse.getUserId()).isNotNull();
        assertThat(loginResponse.getName()).isNotNull();
        assertThat(loginResponse.getEmail()).isNotNull();
    }

    @Then("the login should fail with an invalid credentials error")
    public void the_login_should_fail_with_an_invalid_credentials_error() {
        assertThat(loginException).isNotNull();
        assertThat(loginException).isInstanceOf(InvalidCredentialsException.class);
    }
}
