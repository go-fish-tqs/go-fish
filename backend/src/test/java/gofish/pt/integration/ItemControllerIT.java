package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.ItemDTO;
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

    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        item1 = new Item(1L, "simple rod", "very simple", List.of(), Material.CARBON_FIBER, Category.RODS, 5.0);
        item2 = new Item(2L, "cool rod", "very cool", List.of(), Material.GRAPHITE, Category.RODS, 5.0);

        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void getAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("simple rod"));
    }

    @Test
    void createItem() throws Exception {

        ItemDTO dto1 = new ItemDTO("simple rod", "very simple", List.of(), Category.RODS, Material.CARBON_FIBER, 5.0, 1L);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.name").value("simple rod"))
                .andExpect(header().exists("Location"));

    }

}
