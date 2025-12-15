package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
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

@WebMvcTest(AdminItemController.class)
@Import(SecurityConfig.class)
class AdminItemControllerTest {

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
    @DisplayName("Should return 403 when non-admin tries to access items list")
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/items"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return items list for admin")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnItemsForAdmin() throws Exception {
        // Arrange
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Rod");
        item.setCategory(Category.RODS);
        item.setActive(true);
        when(adminService.getAllItems()).thenReturn(List.of(item));

        // Act & Assert
        mockMvc.perform(get("/api/admin/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Rod"));
    }

    @Test
    @DisplayName("Should deactivate item with reason")
    @WithMockUser(roles = "ADMIN", username = "1")
    void shouldDeactivateItem() throws Exception {
        // Arrange
        doNothing().when(adminService).deactivateItem(anyLong(), anyString(), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/admin/items/100/deactivate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Policy violation\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item deactivated successfully"));

        verify(adminService).deactivateItem(eq(100L), eq("Policy violation"), anyLong());
    }

    @Test
    @DisplayName("Should reactivate item")
    @WithMockUser(roles = "ADMIN", username = "1")
    void shouldReactivateItem() throws Exception {
        // Arrange
        doNothing().when(adminService).reactivateItem(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/admin/items/100/reactivate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item reactivated successfully"));

        verify(adminService).reactivateItem(eq(100L), anyLong());
    }
}
