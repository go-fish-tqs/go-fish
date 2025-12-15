package gofish.pt.boundary;

import gofish.pt.dto.AdminUserDTO;
import gofish.pt.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private AdminUserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = new AdminUserDTO(
                10L, "testuser", "test@example.com", "Lisbon",
                "ROLE_USER", "ACTIVE", null, 5, 3);
    }

    @Test
    @DisplayName("GET /api/admin/users - Should return all users")
    void getAllUsers_returnsUsers() throws Exception {
        when(adminService.getAllUsers()).thenReturn(List.of(testUserDTO));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(adminService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/admin/users - Should return empty list when no users")
    void getAllUsers_returnsEmptyList() throws Exception {
        when(adminService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(adminService).getAllUsers();
    }
}
