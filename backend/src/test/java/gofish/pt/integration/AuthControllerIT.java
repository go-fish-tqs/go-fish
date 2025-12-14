package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private UserRegistrationDTO validRegistrationDTO;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setName("John Doe");
        validRegistrationDTO.setEmail("john.doe@example.com");
        validRegistrationDTO.setPassword("password123");
        validRegistrationDTO.setLocation("Lisboa");
    }

    @Test
    @DisplayName("POST /api/auth/register - Should create user successfully with HTTP 201")
    void register_withValidData_shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.message").value("User created successfully"));

        // Verify user was saved in database
        var users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("John Doe", users.get(0).getUsername());
        assertEquals("john.doe@example.com", users.get(0).getEmail());
        assertEquals("Lisboa", users.get(0).getLocation());
        assertNotEquals("password123", users.get(0).getPassword()); // Password should be hashed
        assertTrue(users.get(0).getPassword().startsWith("$2a$")); // BCrypt hash
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

        // Verify no user was created
        assertEquals(0, userRepository.count());
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

        assertEquals(0, userRepository.count());
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

        assertEquals(0, userRepository.count());
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

        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when email format is invalid")
    void register_withInvalidEmailFormat_shouldReturnBadRequest() throws Exception {
        validRegistrationDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email must be valid"));

        assertEquals(0, userRepository.count());
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

        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when email already exists")
    void register_withDuplicateEmail_shouldReturnConflict() throws Exception {
        // First registration
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated());

        assertEquals(1, userRepository.count());

        // Second registration with same email
        UserRegistrationDTO duplicateDTO = new UserRegistrationDTO();
        duplicateDTO.setName("Jane Doe");
        duplicateDTO.setEmail("john.doe@example.com"); // Same email
        duplicateDTO.setPassword("differentpassword");
        duplicateDTO.setLocation("Porto");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already in use"));

        // Verify only one user exists
        assertEquals(1, userRepository.count());
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

        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when all fields are missing")
    void register_withAllFieldsMissing_shouldReturnBadRequest() throws Exception {
        UserRegistrationDTO emptyDTO = new UserRegistrationDTO();
        emptyDTO.setName("");
        emptyDTO.setEmail("");
        emptyDTO.setPassword("");
        emptyDTO.setLocation("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.location").exists());

        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("POST /api/auth/register - Multiple valid registrations should work")
    void register_multipleUsers_shouldCreateAll() throws Exception {
        // First user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated());

        // Second user with different email
        UserRegistrationDTO secondUser = new UserRegistrationDTO();
        secondUser.setName("Jane Smith");
        secondUser.setEmail("jane.smith@example.com");
        secondUser.setPassword("password456");
        secondUser.setLocation("Porto");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isCreated());

        assertEquals(2, userRepository.count());
    }
}
