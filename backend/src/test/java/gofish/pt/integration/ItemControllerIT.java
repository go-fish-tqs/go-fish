package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.entity.User; // <--- Importa o User
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository; // <--- Precisas disto!
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
    private UserRepository userRepository; // <--- Injeta o repositório dos Users

    @Autowired
    private ObjectMapper objectMapper;

    private User savedOwner; // Para usares o ID gerado nos testes

    @BeforeEach
    void setUp() {
        // 1. Limpeza Geral
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Criar e Salvar um Dono (User)
        User owner = new User();
        owner.setUsername("ze_pescador");
        owner.setEmail("ze@gofish.pt");
        owner.setPassword("12345");
        owner.setLocation("Faro");
        owner.setBalance(0.0);

        savedOwner = userRepository.save(owner); // O Hibernate gera o ID aqui!

        // 3. Criar Itens associados a esse Dono (SEM ID MANUAL!)
        Item item1 = new Item(
                null, // <--- ID NULL (Hibernate gera)
                "simple rod",
                "very simple",
                List.of(),
                Category.RODS,
                Material.CARBON_FIBER,
                5.0,
                true,
                savedOwner, // <--- Associa o dono real
                null
        );

        Item item2 = new Item(
                null, // <--- ID NULL
                "cool rod",
                "very cool",
                List.of(),
                Category.RODS,
                Material.GRAPHITE,
                5.0,
                true,
                savedOwner, // <--- Associa o dono real
                null
        );

        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void getAllItems() throws Exception {
        // Nota: O endpoint deve ser GET /api/items ou POST /api/items/filter com body vazio
        // Vou assumir que o teu endpoint 'filter' aceita vazio para devolver tudo
        mockMvc.perform(post("/api/items/filter"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists()); // A ordem pode variar, verifica só se existe
    }

    @Test
    void createItem() throws Exception {
        // Usa o ID do dono que foi criado no setUp()
        ItemDTO dto1 = new ItemDTO(
                "new rod",
                "brand new",
                List.of(),
                Category.SALTWATER_FLY_RODS,
                Material.CARBON_FIBER,
                5.0,
                savedOwner.getId() // <--- USA O ID REAL GERADO!
        );

        mockMvc.perform(post("/api/items") // Confirma se é este o URL de criar
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated()) // Ou isOk(), depende do teu controller
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("new rod"));
        // .andExpect(header().exists("Location")); // Só descomenta se tiveres implementado o URI location no controller
    }

    @Test
    void getAllItemsWithFilter() throws Exception {
        // Filtra pelo nome "rod" e material CARBON_FIBER (deve sobrar 1: o "simple rod")
        ItemFilter filter = new ItemFilter("rod", Category.RODS, Material.CARBON_FIBER, null ,null, null, null);

        mockMvc.perform(post("/api/items/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("simple rod"));
    }

    @Test
    void getAllCategories() throws Exception {
        var expected = Arrays.stream(Category.values()).filter(Category::isTopLevel).toList();

        mockMvc.perform(get("/api/items/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(expected.size()));
    }

    @Test
    void getAllMaterials() throws Exception {
        mockMvc.perform(get("/api/items/materials")) // O teu controller devolve Map ou List? Ajusta se der erro.
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verifica se o JSON não está vazio, pois a estrutura pode ser complexa (Map)
                .andExpect(jsonPath("$.length()").isNotEmpty());
    }
}