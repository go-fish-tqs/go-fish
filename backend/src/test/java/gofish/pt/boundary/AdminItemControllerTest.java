package gofish.pt.boundary;

import gofish.pt.entity.Item;
import gofish.pt.entity.User;
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

@WebMvcTest(AdminItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private Item testItem;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setUsername("itemowner");

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Fishing Rod");
        testItem.setActive(true);
        testItem.setOwner(testUser);
    }

    @Test
    @DisplayName("GET /api/admin/items - Should return all items")
    void getAllItems_returnsItems() throws Exception {
        when(adminService.getAllItems()).thenReturn(List.of(testItem));

        mockMvc.perform(get("/api/admin/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Fishing Rod"));

        verify(adminService).getAllItems();
    }

    @Test
    @DisplayName("GET /api/admin/items - Should return empty list when no items")
    void getAllItems_returnsEmptyList() throws Exception {
        when(adminService.getAllItems()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(adminService).getAllItems();
    }
}
