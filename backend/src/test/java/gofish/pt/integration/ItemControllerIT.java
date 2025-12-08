package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.*;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Garante que faz rollback no fim de cada teste
class ItemControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository; // Precisamos disto para testar disponibilidade

    private User owner;
    private Item rod;

    @BeforeEach
    void setUp() {
        // 1. Limpar o convés
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Criar o Dono
        owner = new User();
        owner.setUsername("ze_pescador");
        owner.setEmail("ze@gofish.pt");
        owner.setPassword("pass123");
        owner.setLocation("Lagos");
        owner.setBalance(0.0);
        owner = userRepository.save(owner); // O ID é gerado aqui

        // 3. Criar um Item para testes de leitura
        rod = new Item();
        rod.setName("Cana de Surfcasting");
        rod.setDescription("Lança longe");
        rod.setPrice(15.0);
        rod.setCategory(Category.RODS);
        rod.setMaterial(Material.CARBON_FIBER);
        rod.setOwner(owner);
        rod.setAvailable(true);
        rod = itemRepository.save(rod); // O ID é gerado aqui
    }

    @Test
    @DisplayName("POST /api/items - Deve criar um item novo")
    void createItem() throws Exception {
        // Arrange
        ItemDTO dto = new ItemDTO(
                "Novo Barco",
                "Barco rápido",
                List.of("http://foto.com/1.jpg"),
                Category.BOATS,
                Material.FIBERGLASS_BOAT,
                100.0,
                owner.getId() // <--- Usa o ID real do dono
        );

        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo Barco"))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("GET /api/items/{id} - Deve devolver o item correto")
    void getItem() throws Exception {
        mockMvc.perform(get("/api/items/{id}", rod.getId())) // Passa o ID gerado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cana de Surfcasting"));
    }

    @Test
    @DisplayName("POST /api/items/filter - Deve filtrar items")
    void filterItems() throws Exception {
        // Filtra por categoria RODS (deve encontrar a cana criada no setUp)
        ItemFilter filter = new ItemFilter(null, Category.RODS, null, null, null, null, null);

        mockMvc.perform(post("/api/items/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Cana de Surfcasting"));
    }

    @Test
    @DisplayName("GET /api/items/{id}/availability - Deve devolver datas ocupadas")
    void checkAvailability() throws Exception {
        // 1. Criar uma reserva CONFIRMADA para "bloquear" uns dias
        Booking booking = new Booking();
        booking.setItem(rod);
        booking.setUser(owner); // O próprio dono reservou (só para teste)
        booking.setStartDate(LocalDateTime.now().plusDays(2).withHour(0).withMinute(0)); // Daqui a 2 dias
        booking.setEndDate(LocalDateTime.now().plusDays(3).withHour(23).withMinute(59)); // Até daqui a 3 dias
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // 2. Pedir disponibilidade para a próxima semana
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(5);

        // Act & Assert
        mockMvc.perform(get("/api/items/{id}/availability", rod.getId())
                .param("from", from.toString()) // 2025-XX-XX
                .param("to", to.toString()))
                .andExpect(status().isOk())
                // Esperamos que devolva os dias da reserva (Dia+2 e Dia+3)
                // Pode devolver mais se houver dias passados no meio, mas pelo menos esses 2
                .andExpect(jsonPath("$.length()").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/items/categories - Deve devolver apenas categorias de topo")
    void getCategories() throws Exception {
        mockMvc.perform(get("/api/items/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(7))) // 7 top-level categories
                .andExpect(jsonPath("$[?(@.id == 'RODS')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'REELS')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'COMBOS')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'ELECTRONICS')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'APPAREL')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'ACCESSORIES')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'BOATS')]").exists());
    }

    @Test
    @DisplayName("GET /api/items/materials - Deve devolver materiais agrupados por grupo")
    void getMaterials() throws Exception {
        mockMvc.perform(get("/api/items/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RODS").isArray())
                .andExpect(jsonPath("$.REELS").isArray())
                .andExpect(jsonPath("$.BOATS").isArray())
                .andExpect(jsonPath("$.APPARELS").isArray())
                .andExpect(jsonPath("$.ACCESSORIES").isArray())
                .andExpect(jsonPath("$.NETS").isArray());
    }
}