package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.AdminUserDTO;
import gofish.pt.service.AdminService;
import gofish.pt.config.SecurityConfig;
import gofish.pt.security.JwtAuthenticationFilter;
import gofish.pt.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should return 403 when non-admin tries to access users list")
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return users list for admin")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUsersForAdmin() throws Exception {
        // Arrange
        AdminUserDTO user = new AdminUserDTO(
                1L, "testuser", "test@example.com", "Location",
                "USER", "ACTIVE", null, 5, 3);
        when(adminService.getAllUsers()).thenReturn(List.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    @DisplayName("Should suspend user successfully")
    @WithMockUser(roles = "ADMIN", username = "1") // Admin userId = 1
    void shouldSuspendUser() throws Exception {
        // Arrange
        doNothing().when(adminService).suspendUser(anyLong(), anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/admin/users/10/suspend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Terms violation\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User suspended successfully"));

        verify(adminService).suspendUser(eq(10L), anyLong(), eq("Terms violation"));
    }

    @Test
    @DisplayName("Should reactivate user successfully")
    @WithMockUser(roles = "ADMIN", username = "1")
    void shouldReactivateUser() throws Exception {
        // Arrange
        doNothing().when(adminService).reactivateUser(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/admin/users/10/reactivate"))
                .andExpect(status().isOk())
                .andExpect(content().string("User reactivated successfully"));

        verify(adminService).reactivateUser(eq(10L), anyLong());
    }

    @Test
    @DisplayName("Should soft delete user successfully")
    @WithMockUser(roles = "ADMIN", username = "1")
    void shouldSoftDeleteUser() throws Exception {
        // Arrange
        doNothing().when(adminService).softDeleteUser(anyLong(), anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/admin/users/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Account closure\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(adminService).softDeleteUser(eq(10L), anyLong(), eq("Account closure"));
    }
}
