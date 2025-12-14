package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.config.TestSecurityConfig;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.repository.UserRepository; // Importar Repositório
import gofish.pt.service.BookingService;
import gofish.pt.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(TestSecurityConfig.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private BookingService bookingService;

    // CORREÇÃO: Adicionar o Mock do UserRepository
    // O Controller agora precisa disto para funcionar, mesmo que o teste não o use explicitamente.
    @MockitoBean 
    private UserRepository userRepository;

    private Item testItem;
    private ItemDTO testDto;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Rod");
        testItem.setDescription("A test fishing rod");
        testItem.setCategory(Category.RODS);
        testItem.setMaterial(Material.GRAPHITE);
        testItem.setPrice(25.99);
        testItem.setAvailable(true);

        testDto = new ItemDTO(
                "Test Rod",
                "A test fishing rod",
                List.of("http://example.com/photo.jpg"),
                Category.RODS,
                Material.GRAPHITE,
                25.99,
                1L);
    }

    @Test
    @DisplayName("POST /api/items/filter - Should return filtered items")
    void getItems_withFilter_returnsFilteredItems() throws Exception {
        ItemFilter filter = new ItemFilter("rod", null, null, null, null, null, null);
        when(itemService.findAll(any(ItemFilter.class))).thenReturn(List.of(testItem));

        mockMvc.perform(post("/api/items/filter")
                .with(user("user"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Rod"));

        verify(itemService).findAll(any(ItemFilter.class));
    }

    @Test
    @DisplayName("POST /api/items/filter - Should return all items when filter is null")
    void getItems_withNullFilter_returnsAllItems() throws Exception {
        when(itemService.findAll((ItemFilter) null)).thenReturn(List.of(testItem));

        mockMvc.perform(post("/api/items/filter")
                .with(user("user"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemService).findAll((ItemFilter) null);
    }

    @Test
    @DisplayName("GET /api/items/{id} - Should return item when found")
    void getItem_whenExists_returnsItem() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(testItem));

        mockMvc.perform(get("/api/items/{id}", 1)
                .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Rod"));

        verify(itemService).findById(1L);
    }

    @Test
    @DisplayName("GET /api/items/{id} - Should return 404 when item not found")
    void getItem_whenNotExists_returns404() throws Exception {
        when(itemService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/{id}", 999)
                .with(user("user")))
                .andExpect(status().isNotFound());

        verify(itemService).findById(999L);
    }

    @Test
    @DisplayName("POST /api/items - Should create item and return 201")
    void createItem_withValidDto_createsAndReturns201() throws Exception {
        when(itemService.save(any(ItemDTO.class))).thenReturn(testItem);

        mockMvc.perform(post("/api/items")
                .with(user("user"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/items/1"))
                .andExpect(jsonPath("$.name").value("Test Rod"));

        verify(itemService).save(any(ItemDTO.class));
    }

    @Test
    @DisplayName("GET /api/items/categories - Should return only top-level categories")
    void getCategories_returnsTopLevelCategories() throws Exception {
        List<Category> topLevelCategories = Arrays.stream(Category.values())
                .filter(Category::isTopLevel)
                .toList();
        when(itemService.getCategories()).thenReturn(topLevelCategories);

        mockMvc.perform(get("/api/items/categories")
                .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));

        verify(itemService).getCategories();
    }

    @Test
    @DisplayName("GET /api/items/materials - Should return materials grouped by MaterialGroup")
    void getMaterials_returnsMaterialsGroupedByGroup() throws Exception {
        Map<Material.MaterialGroup, List<Material>> materialsMap = new HashMap<>();
        materialsMap.put(Material.MaterialGroup.RODS, List.of(Material.GRAPHITE, Material.FIBERGLASS));
        materialsMap.put(Material.MaterialGroup.REELS, List.of(Material.ALUMINUM, Material.BRASS));
        when(itemService.getMaterials()).thenReturn(materialsMap);

        mockMvc.perform(get("/api/items/materials")
                .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RODS", hasSize(2)));

        verify(itemService).getMaterials();
    }

    @Test
    @DisplayName("GET /api/items/materials - Should return all material groups")
    void getMaterials_returnsAllMaterialGroups() throws Exception {
        Map<Material.MaterialGroup, List<Material>> materialsMap = new HashMap<>();
        for (Material.MaterialGroup group : Material.MaterialGroup.values()) {
            materialsMap.put(group, group.getMaterials());
        }
        when(itemService.getMaterials()).thenReturn(materialsMap);

        mockMvc.perform(get("/api/items/materials")
                .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RODS").exists());

        verify(itemService).getMaterials();
    }
}