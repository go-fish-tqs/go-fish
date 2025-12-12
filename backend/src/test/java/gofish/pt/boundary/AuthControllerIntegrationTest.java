package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.UserRegistrationRequest;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    void register_success_createsUserAndHashesPassword() throws Exception {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("ze_pescador");
        req.setEmail("ze@example.com");
        req.setPassword("secret123");
        req.setLocation("Lisboa");

        String json = objectMapper.writeValueAsString(req);

        var result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andReturn();

        // verify in DB
        var opt = userRepository.findByEmail("ze@example.com");
        assertThat(opt).isPresent();
        User saved = opt.get();
        assertThat(saved.getUsername()).isEqualTo("ze_pescador");
        assertThat(saved.getLocation()).isEqualTo("Lisboa");
        // password must be hashed (not equal to raw)
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(saved.getPassword()).isNotEmpty();
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        User existing = new User();
        existing.setUsername("someone");
        existing.setEmail("dup@example.com");
        existing.setPassword("x");
        existing.setLocation("X");
        existing.setBalance(0.0);
        userRepository.save(existing);

        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("newuser");
        req.setEmail("dup@example.com");
        req.setPassword("pwd12345");
        req.setLocation("Porto");

        String json = objectMapper.writeValueAsString(req);

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void register_missingField_returnsBadRequest() throws Exception {
        // missing location
        String json = "{\"username\":\"u\",\"email\":\"a@b.com\",\"password\":\"p\"}";

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_malformedJson_returnsBadRequest() throws Exception {
        String bad = "{ this is not json";

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bad))
                .andExpect(status().isBadRequest());
    }
}
