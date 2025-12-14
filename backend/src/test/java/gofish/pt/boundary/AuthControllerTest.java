package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.config.SecurityConfig;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.entity.User;
import gofish.pt.exception.DuplicateEmailException;
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
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserRegistrationDTO validRegistrationDTO;
    private User testUser;

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
    }

    @Test
    @DisplayName("POST /api/auth/register - Should create user and return 201")
    void register_withValidData_shouldReturnCreated() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.message").value("User created successfully"));

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
    @DisplayName("POST /api/auth/register - Should return 400 when password is too short")
    void register_withShortPassword_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setPassword("12345");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 6 characters"));

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
}
