package gofish.pt.boundary;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.ItemUpdateDTO;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.entity.User;
import gofish.pt.config.TestSecurityConfig;
import gofish.pt.repository.UserRepository;
import gofish.pt.service.BookingService;
import gofish.pt.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ItemControllerUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserRepository userRepository;

    private Item testItem;
    private User testOwner;

    @BeforeEach
    void setUp() {
        testOwner = new User();
        testOwner.setId(1L);
        testOwner.setUsername("testuser");
        testOwner.setEmail("test@example.com");

        // Mock UserRepository to return testOwner when looking up by username
        // Use lenient() to ensure the mock is available for all tests
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testOwner));

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Rod");
        testItem.setDescription("A test fishing rod");
        testItem.setCategory(Category.RODS);
        testItem.setMaterial(Material.GRAPHITE);
        testItem.setPrice(25.99);
        testItem.setAvailable(true);
        testItem.setOwner(testOwner);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/items/{id} - Should update item when owner")
    @Requirement("GF-UPDATE-01")
    void updateItem_whenOwner_returnsUpdatedItem() throws Exception {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("Updated Rod");
        updateDTO.setPrice(35.99);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Rod");
        updatedItem.setDescription("A test fishing rod");
        updatedItem.setCategory(Category.RODS);
        updatedItem.setMaterial(Material.GRAPHITE);
        updatedItem.setPrice(35.99);
        updatedItem.setAvailable(true);
        updatedItem.setOwner(testOwner);

        when(itemService.updateItem(eq(1L), any(ItemUpdateDTO.class), eq(1L))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/{id}", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Rod"))
                .andExpect(jsonPath("$.price").value(35.99));

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDTO.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/items/{id} - Should return 404 when item not found")
    @Requirement("GF-UPDATE-02")
    void updateItem_whenNotFound_returns404() throws Exception {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("Updated Rod");

        when(itemService.updateItem(eq(999L), any(ItemUpdateDTO.class), eq(1L)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Item not found"));

        mockMvc.perform(put("/api/items/{id}", 999)

                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(itemService).updateItem(eq(999L), any(ItemUpdateDTO.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "otheruser")
    @DisplayName("PUT /api/items/{id} - Should return 403 when not owner")
    @Requirement("GF-UPDATE-03")
    void updateItem_whenNotOwner_returns403() throws Exception {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("Updated Rod");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        when(itemService.updateItem(eq(1L), any(ItemUpdateDTO.class), eq(2L)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Only the item owner can update this item"));

        mockMvc.perform(put("/api/items/{id}", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDTO.class), eq(2L));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/items/{id} - Should support partial updates")
    @Requirement("GF-UPDATE-04")
    void updateItem_withPartialData_updatesOnlyProvidedFields() throws Exception {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setPrice(45.99); // Only updating price

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Test Rod"); // Name unchanged
        updatedItem.setDescription("A test fishing rod"); // Description unchanged
        updatedItem.setCategory(Category.RODS);
        updatedItem.setMaterial(Material.GRAPHITE);
        updatedItem.setPrice(45.99); // Price updated
        updatedItem.setAvailable(true);
        updatedItem.setOwner(testOwner);

        when(itemService.updateItem(eq(1L), any(ItemUpdateDTO.class), eq(1L))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/{id}", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(45.99))
                .andExpect(jsonPath("$.name").value("Test Rod")); // Original name preserved

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDTO.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/items/{id} - Should validate price is positive")
    @Requirement("GF-UPDATE-05")
    void updateItem_withNegativePrice_returnsBadRequest() throws Exception {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setPrice(-10.0);

        mockMvc.perform(put("/api/items/{id}", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/items/{id} - Should allow updating photos")
    @Requirement("GF-UPDATE-06")
    void updateItem_withNewPhotos_updatesPhotosList() throws Exception {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setPhotoUrls(java.util.List.of("http://example.com/new1.jpg", "http://example.com/new2.jpg"));

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Test Rod");
        updatedItem.setDescription("A test fishing rod");
        updatedItem.setCategory(Category.RODS);
        updatedItem.setMaterial(Material.GRAPHITE);
        updatedItem.setPrice(25.99);
        updatedItem.setAvailable(true);
        updatedItem.setOwner(testOwner);
        updatedItem.setPhotoUrls(java.util.List.of("http://example.com/new1.jpg", "http://example.com/new2.jpg"));

        when(itemService.updateItem(eq(1L), any(ItemUpdateDTO.class), eq(1L))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/{id}", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrls").isArray())
                .andExpect(jsonPath("$.photoUrls[0]").value("http://example.com/new1.jpg"));

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDTO.class), eq(1L));
    }
}
