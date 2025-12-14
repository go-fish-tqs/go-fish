package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.BlockDateRequestDTO;
import gofish.pt.dto.ItemDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.entity.*;
import gofish.pt.repository.BlockedDateRepository;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BlockedDateRepository blockedDateRepository;

    private User owner;
    private User nonOwner;
    private Item rod;

    @BeforeEach
    void setUp() {
        // 1. Limpar a base de dados
        bookingRepository.deleteAll();
        blockedDateRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Criar os Utilizadores
        owner = new User();
        owner.setUsername("ze_pescador");
        owner.setEmail("ze@gofish.pt");
        owner.setPassword("pass123");
        owner.setLocation("Lagos");
        owner.setBalance(0.0);
        owner = userRepository.saveAndFlush(owner);

        nonOwner = new User();
        nonOwner.setUsername("turista_joao");
        nonOwner.setEmail("joao@gofish.pt");
        nonOwner.setPassword("pass456");
        nonOwner.setLocation("Faro");
        nonOwner.setBalance(0.0);
        nonOwner = userRepository.saveAndFlush(nonOwner);

        // 3. Criar um Item para testes
        rod = new Item();
        rod.setName("Cana de Surfcasting");
        rod.setDescription("Lança longe e é muito resistente"); // Descrição válida
        rod.setPrice(15.0);
        rod.setCategory(Category.RODS);
        rod.setMaterial(Material.CARBON_FIBER);
        rod.setOwner(owner);
        rod.setAvailable(true);
        rod = itemRepository.saveAndFlush(rod);
    }

    @Test
    @DisplayName("POST /api/items - Deve criar um item novo")
    void createItem() throws Exception {
        ItemDTO dto = new ItemDTO(
                "Novo Barco",
                "Barco rápido para pesca em alto mar",
                List.of("http://foto.com/1.jpg"),
                Category.BOATS,
                Material.FIBERGLASS_BOAT,
                100.0,
                owner.getId()
        );

        mockMvc.perform(post("/api/items")
                .with(user(owner.getUsername()).password(owner.getPassword())) // Autenticação
                .with(csrf()) // Token CSRF para POST
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo Barco"))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("GET /api/items/{id} - Deve devolver o item correto")
    void getItem() throws Exception {
        mockMvc.perform(get("/api/items/{id}", rod.getId())
                .with(user("qualquer_user"))) // Simula user logado para passar o 401
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cana de Surfcasting"));
    }

    @Test
    @DisplayName("POST /api/items/filter - Deve filtrar items")
    void filterItems() throws Exception {
        ItemFilter filter = new ItemFilter(null, Category.RODS, null, null, null, null, null);

        mockMvc.perform(post("/api/items/filter")
                .with(user("qualquer_user")) // Autenticação
                .with(csrf()) // Token CSRF para POST
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Cana de Surfcasting"));
    }

    @Test
    @DisplayName("GET /api/items/{id}/availability - Deve devolver datas ocupadas por reservas")
    void checkAvailability_withBookings() throws Exception {
        Booking booking = new Booking();
        booking.setItem(rod);
        booking.setUser(owner);
        booking.setStartDate(LocalDate.now().plusDays(2));
        booking.setEndDate(LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.saveAndFlush(booking);

        // 2. Pedir disponibilidade para a próxima semana
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(5);

        // Act & Assert
        mockMvc.perform(get("/api/items/{id}/unavailability", rod.getId())
                .param("from", from.toString()) // 2025-XX-XX
                .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());
    }

    @Test
    void getItemById() throws Exception {
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.name").value("Cana de Surfcasting"));
    }

    @Test
    void getItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/items/67"))
                .andExpect(status().isNotFound());

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
        @DisplayName("POST /items/{itemId}/blocked-dates - Should block dates for owner")
        void blockDate_Success() throws Exception {
            BlockDateRequestDTO dto = new BlockDateRequestDTO();
            dto.setStartDate(LocalDate.now().plusDays(10));
            dto.setEndDate(LocalDate.now().plusDays(12));
            dto.setReason("Owner vacation");

            mockMvc.perform(post("/api/items/{itemId}/blocked-dates", rod.getId())
                    .with(user(owner.getUsername()).password(owner.getPassword())) // Autenticado como DONO
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reason", is("Owner vacation")));
        }

        @Test
        @DisplayName("POST /items/{itemId}/blocked-dates - Should return 409 when conflict with booking exists")
        void blockDate_whenConflict_then409() throws Exception {
            Booking booking = new Booking();
            booking.setItem(rod);
            booking.setUser(nonOwner);
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(7);
            booking.setStartDate(startDate);
            booking.setEndDate(endDate);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.saveAndFlush(booking);

            BlockDateRequestDTO dto = new BlockDateRequestDTO();
            dto.setStartDate(LocalDate.now().plusDays(6));
            dto.setEndDate(LocalDate.now().plusDays(8));

            mockMvc.perform(post("/api/items/{itemId}/blocked-dates", rod.getId())
                            .with(user(owner.getUsername()).password(owner.getPassword())) // Autenticado como DONO
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST /items/{itemId}/blocked-dates - Should return 403 when user is not owner")
        void blockDate_whenNotOwner_then403() throws Exception {
            // Arrange: Criar item pertencente ao "nonOwner"
            Item otherItem = new Item();
            otherItem.setOwner(nonOwner);
            otherItem.setName("Other Item");
            otherItem.setDescription("Uma descrição válida obrigatória"); // CORREÇÃO DO ERRO DE VALIDAÇÃO
            otherItem.setCategory(Category.BOATS);
            otherItem.setMaterial(Material.FIBERGLASS_BOAT);
            otherItem.setPrice(50.0);
            otherItem.setAvailable(true);
            otherItem = itemRepository.saveAndFlush(otherItem);

            BlockDateRequestDTO dto = new BlockDateRequestDTO();
            dto.setStartDate(LocalDate.now().plusDays(10));
            dto.setEndDate(LocalDate.now().plusDays(12));

            // Act & Assert: Tentamos bloquear datas usando as credenciais do "owner" (que não é dono deste item)
            mockMvc.perform(post("/api/items/{itemId}/blocked-dates", otherItem.getId())
                            .with(user(owner.getUsername()).password(owner.getPassword())) // User errado
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /items/blocked-dates/{blockedDateId} - Should delete blocked date for owner")
        void deleteBlockDate_Success() throws Exception {
            BlockedDate blockedDate = new BlockedDate(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "test", rod);
            blockedDate = blockedDateRepository.saveAndFlush(blockedDate);

            mockMvc.perform(delete("/api/items/blocked-dates/{blockedDateId}", blockedDate.getId())
                    .with(user(owner.getUsername()).password(owner.getPassword())) // Autenticado como DONO
                    .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /items/blocked-dates/{blockedDateId} - Should return 403 when user is not owner")
        void deleteBlockDate_whenNotOwner_then403() throws Exception {
            Item otherItem = new Item();
            otherItem.setOwner(nonOwner);
            otherItem.setName("Other Item");
            otherItem.setDescription("Descrição válida obrigatória"); // Garantir que não está blank
            otherItem.setCategory(Category.BOATS);
            otherItem.setMaterial(Material.FIBERGLASS_BOAT);
            otherItem.setPrice(50.0);
            otherItem.setAvailable(true);
            otherItem = itemRepository.saveAndFlush(otherItem);

            BlockedDate blockedDate = new BlockedDate(LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), "test", otherItem);
            blockedDate = blockedDateRepository.saveAndFlush(blockedDate);

            mockMvc.perform(delete("/api/items/blocked-dates/{blockedDateId}", blockedDate.getId())
                    .with(user(owner.getUsername()).password(owner.getPassword())) // User errado
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }

    @Test
    @DisplayName("GET /availability - Should include manually blocked dates")
    void checkAvailability_includesBlockedDates() throws Exception {
        LocalDate blockedDate = LocalDate.now().plusDays(4);
        BlockedDate period = new BlockedDate(blockedDate, blockedDate, "maintenance", rod);
        blockedDateRepository.saveAndFlush(period);

        mockMvc.perform(get("/api/items/{id}/availability", rod.getId())
                        .with(user("qualquer_user")) // Autenticação
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().plusDays(5).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@ == '%s')]", blockedDate.toString()).exists());
    }
}