package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.dto.ItemDTO;
import gofish.pt.entity.Material;
import gofish.pt.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        Item item1 = new Item(1L, "simple rod", "very simple", List.of(), Material.CARBON_FIBER, Category.RODS, 5.0);
        Item item2 = new Item(2L, "cool rod", "very cool", List.of(), Material.GRAPHITE, Category.RODS, 5.0);

        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void getAllItems() throws Exception {
        mockMvc.perform(post("/api/items/filter"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("simple rod"));
    }

    @Test
    void createItem() throws Exception {

        ItemDTO dto1 = new ItemDTO("simple rod", "very simple", List.of(), Category.SALTWATER_FLY_RODS, Material.CARBON_FIBER, 5.0, 1L);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.name").value("simple rod"))
                .andExpect(header().exists("Location"));

    }

    @Test
    void getAllItemsWithFilter() throws Exception {

        ItemFilter filter = new ItemFilter("rod", Category.RODS, Material.CARBON_FIBER, null ,null, null, null);

        mockMvc.perform(post("/api/items/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("simple rod"));
    }


    @Test
    void getAllCategories() throws Exception {

        var expected = Arrays.stream(Category.values()).filter(Category::isTopLevel).toList();

        mockMvc.perform(get("/api/items/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(expected.size()));
    }

    @Test
    void getAllMaterials() throws Exception {

        mockMvc.perform(get("/api/items/materials"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(Material.MaterialGroup.values().length));
    }

}
