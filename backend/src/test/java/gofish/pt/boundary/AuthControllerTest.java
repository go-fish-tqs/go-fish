package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.config.TestSecurityConfig;
import gofish.pt.dto.LoginRequestDTO;
import gofish.pt.dto.LoginResponseDTO;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.exception.InvalidCredentialsException;
import gofish.pt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserRegistrationDTO validRegistrationDTO;
    private User testUser;
    private LoginRequestDTO validLoginRequest;
    private LoginResponseDTO loginResponse;

    @BeforeEach
    void setUp() {
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setName("John Doe");
        validRegistrationDTO.setEmail("john.doe@example.com");
        validRegistrationDTO.setPassword("password123");
        validRegistrationDTO.setLocation("Lisboa");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setLocation("Lisboa");
        testUser.setBalance(0.0);

        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("john.doe@example.com");
        validLoginRequest.setPassword("password123");

        loginResponse = new LoginResponseDTO();
        loginResponse.setUserId(1L);
        loginResponse.setToken("mock-jwt-token");
        loginResponse.setName("John Doe");
        loginResponse.setEmail("john.doe@example.com");
    }

    // ========== REGISTRATION TESTS ==========

    @Test
    @DisplayName("POST /api/auth/register - Should create user and return 201")
    void register_withValidData_shouldReturnCreated() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when name is missing")
    void register_withMissingName_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setName("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when email is missing")
    void register_withMissingEmail_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setEmail("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when password is missing")
    void register_withMissingPassword_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when location is missing")
    void register_withMissingLocation_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setLocation("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.location").exists());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when email format is invalid")
    void register_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email must be valid"));

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when email already exists")
    void register_withDuplicateEmail_shouldReturnConflict() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new DuplicateEmailException("Email already in use"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already in use"));

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for malformed JSON")
    void register_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{name: 'John', invalid}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request"));

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("POST /api/auth/login - Should login successfully with valid credentials")
    void login_withValidCredentials_shouldReturn200() throws Exception {
        when(userService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 when email is missing")
    void login_withMissingEmail_shouldReturnBadRequest() throws Exception {
        validLoginRequest.setEmail("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verify(userService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 when password is missing")
    void login_withMissingPassword_shouldReturnBadRequest() throws Exception {
        validLoginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());

        verify(userService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 with invalid credentials")
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));

        verify(userService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for malformed JSON")
    void login_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{email: 'test', invalid}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request"));

        verify(userService, never()).login(any(LoginRequestDTO.class));
    }
}
