package gofish.pt.bdd.stepdefinitions;

import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.User;
import gofish.pt.entity.UserRole;
import gofish.pt.entity.UserStatus;
import gofish.pt.exception.DuplicateEmailException;
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

public class RegisterSteps {

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

    private User registeredUser;
    private Exception registrationException;

    @Before
    public void setUp() {
        registeredUser = null;
        registrationException = null;
    }

    @When("I register with name {string} email {string} password {string} and location {string}")
    public void i_register_with_name_email_password_and_location(String name, String email, String password,
            String location) {
        UserRegistrationDTO dto = new UserRegistrationDTO(name, email, password, location);
        try {
            registeredUser = userService.registerUser(dto);
        } catch (DuplicateEmailException e) {
            registrationException = e;
        }
    }

    @Then("the registration should be successful")
    public void the_registration_should_be_successful() {
        assertThat(registrationException).isNull();
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isNotNull();
    }

    @Then("the user should have role {string}")
    public void the_user_should_have_role(String expectedRole) {
        String actualRole = userRoleRepository.findByUserId(registeredUser.getId())
                .map(UserRole::getRole)
                .orElse(null);
        assertThat(actualRole).isEqualTo(expectedRole);
    }

    @Then("the user should have status {string}")
    public void the_user_should_have_status(String expectedStatus) {
        String actualStatus = userStatusRepository.findByUserId(registeredUser.getId())
                .map(UserStatus::getStatus)
                .orElse(null);
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Then("the user should have balance {double}")
    public void the_user_should_have_balance(Double expectedBalance) {
        assertThat(registeredUser.getBalance()).isEqualTo(expectedBalance);
    }

    @Then("the registration should fail with a duplicate email error")
    public void the_registration_should_fail_with_a_duplicate_email_error() {
        assertThat(registrationException).isNotNull();
        assertThat(registrationException).isInstanceOf(DuplicateEmailException.class);
    }
}
