package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.UserResponseDTO;
import gofish.pt.dto.UserUpdateDTO;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponseDTO userResponseDTO;
    private UserUpdateDTO userUpdateDTO;

    @BeforeEach
    void setUp() {
        userResponseDTO = new UserResponseDTO(
                1L,
                "testuser",
                "test@example.com",
                "+351912345678",
                "Rua Test 123",
                "https://example.com/photo.jpg",
                "Porto",
                100.0
        );

        userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUsername("updateduser");
        userUpdateDTO.setEmail("updated@example.com");
        userUpdateDTO.setPhone("+351987654321");
        userUpdateDTO.setAddress("Rua Updated 456");
        userUpdateDTO.setProfilePhoto("https://example.com/new-photo.jpg");
        userUpdateDTO.setLocation("Lisboa");
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_WhenValidData_ShouldReturnUpdatedUser() throws Exception {
        when(userService.updateUser(eq(1L), eq(1L), any(UserUpdateDTO.class)))
                .thenReturn(userResponseDTO);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phone").value("+351912345678"))
                .andExpect(jsonPath("$.address").value("Rua Test 123"))
                .andExpect(jsonPath("$.location").value("Porto"));
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_WhenDuplicateEmail_ShouldReturn409() throws Exception {
        when(userService.updateUser(eq(1L), eq(1L), any(UserUpdateDTO.class)))
                .thenThrow(new DuplicateEmailException("Email already in use"));

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    @WithMockUser(username = "2")
    void updateUser_WhenUserIdMismatch_ShouldReturn404() throws Exception {
        when(userService.updateUser(eq(1L), eq(2L), any(UserUpdateDTO.class)))
                .thenThrow(new IllegalArgumentException("You can only update your own profile"));

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("You can only update your own profile"));
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_WhenInvalidEmail_ShouldReturn400() throws Exception {
        userUpdateDTO.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_WhenInvalidPhone_ShouldReturn400() throws Exception {
        userUpdateDTO.setPhone("abc");

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1")
    void getUserProfile_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getUserProfile(1L)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phone").value("+351912345678"));
    }

    @Test
    @WithMockUser(username = "1")
    void getUserProfile_WhenUserNotFound_ShouldReturn404() throws Exception {
        when(userService.getUserProfile(99L))
                .thenThrow(new IllegalArgumentException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: 99"));
    }
}
