package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User renter;
    private Item rod;
    private Item kayak;
    private Booking ownedBooking;
    private Booking rentalBooking;

    @BeforeEach
    void setUp() {
        // Limpeza da casa
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Criar e Gravar o Owner
        owner = new User();
        owner.setUsername("pescador_owner");
        owner.setEmail("owner@gofish.pt");
        owner.setPassword("pass123");
        owner.setLocation("Lagos");
        owner.setBalance(0.0);
        owner = userRepository.save(owner);

        // 2. Criar e Gravar o Renter
        renter = new User();
        renter.setUsername("turista_renter");
        renter.setEmail("renter@gofish.pt");
        renter.setPassword("pass123");
        renter.setLocation("Porto");
        renter.setBalance(100.0);
        renter = userRepository.save(renter);

        // 3. Criar Items (AQUI ESTAVA O GATO!)
        rod = new Item();
        rod.setName("Cana de Surfcasting");
        rod.setDescription("Lança longe");
        rod.setPrice(15.0);
        rod.setCategory(Category.RODS);
        rod.setMaterial(Material.CARBON_FIBER);
        rod.setAvailable(true);

        // OBRIGATÓRIO: Usar o addItem para o owner saber que tem isto na lista dele em memória
        owner.addItem(rod);
        rod = itemRepository.save(rod); // Gravar o item (o owner é atualizado por cascata ou referência)

        kayak = new Item();
        kayak.setName("Kayak Duplo");
        kayak.setDescription("Para passear");
        kayak.setPrice(25.0);
        kayak.setCategory(Category.BOATS);
        kayak.setMaterial(Material.ROTOMOLDED_POLYETHYLENE);
        kayak.setAvailable(true);

        // OBRIGATÓRIO: Atualizar a lista do owner outra vez
        owner.addItem(kayak);
        kayak = itemRepository.save(kayak);

        // 4. Criar Bookings
        ownedBooking = new Booking();
        ownedBooking.setStartDate(LocalDate.now());
        ownedBooking.setEndDate(LocalDate.now().plusDays(1));
        ownedBooking.setStatus(BookingStatus.CONFIRMED);

        // Ligar tudo nos dois sentidos
        renter.addBooking(ownedBooking); // Renter fica a saber que tem booking
        rod.addBooking(ownedBooking);    // Item fica a saber que tem booking
        ownedBooking = bookingRepository.save(ownedBooking);

        // 5. Criar segundo booking
        rentalBooking = new Booking();
        rentalBooking.setStartDate(LocalDate.now().plusDays(2));
        rentalBooking.setEndDate(LocalDate.now().plusDays(3));
        rentalBooking.setStatus(BookingStatus.PENDING);

        // Ligar tudo nos dois sentidos
        renter.addBooking(rentalBooking);
        kayak.addBooking(rentalBooking);
        rentalBooking = bookingRepository.save(rentalBooking);

        // DICA EXTRA DE AMIGO:
        // Se quiseres ter a certeza absoluta que a base de dados está sincronizada
        // e que o teste não está a usar lixo da memória, podias injetar o EntityManager
        // e fazer: entityManager.flush(); entityManager.clear();
        // Mas com o código acima, já deve bulir!
    }

    @Test
    @DisplayName("GET /api/users/{id}/bookings - Should return all bookings for a user")
    void getUserBookings_shouldReturnBookingsWhenUserExists() throws Exception {
        mockMvc.perform(get("/api/users/{id}/bookings", renter.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(ownedBooking.getId()))
                .andExpect(jsonPath("$[1].id").value(rentalBooking.getId()));
    }

    @Test
    @DisplayName("GET /api/users/{id}/bookings - Should return 404 when user not found")
    void getUserBookings_shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}/bookings", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: 9999"));
    }

    @Test
    @DisplayName("GET /api/users/{id}/bookings - Should return empty list when user has no bookings")
    void getUserBookings_shouldReturnEmptyListWhenNoBookings() throws Exception {
        // Create a new user with no bookings
        User noBookingUser = new User();
        noBookingUser.setUsername("lonely_user");
        noBookingUser.setEmail("lonely@gofish.pt");
        noBookingUser.setPassword("pass123");
        noBookingUser.setLocation("Somewhere");
        noBookingUser.setBalance(50.0);
        noBookingUser = userRepository.save(noBookingUser);

        mockMvc.perform(get("/api/users/{id}/bookings", noBookingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-bookings - Should return all bookings for owned items")
    void getUserOwnedBookings_shouldReturnOwnedBookingsWhenUserIsOwner() throws Exception {
        mockMvc.perform(get("/api/users/{id}/owned-bookings", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(ownedBooking.getId()))
                .andExpect(jsonPath("$[1].id").value(rentalBooking.getId()));
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-bookings - Should return 404 when user not found")
    void getUserOwnedBookings_shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}/owned-bookings", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: 9999"));
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-bookings - Should return empty list when user has no items")
    void getUserOwnedBookings_shouldReturnEmptyListWhenNoOwnedItems() throws Exception {
        // Renter has no owned items
        mockMvc.perform(get("/api/users/{id}/owned-bookings", renter.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-items - Should return all items owned by user")
    void getUserOwnedItems_shouldReturnOwnedItemsWhenUserIsOwner() throws Exception {
        mockMvc.perform(get("/api/users/{id}/owned-items", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Cana de Surfcasting"))
                .andExpect(jsonPath("$[1].name").value("Kayak Duplo"));
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-items - Should return 404 when user not found")
    void getUserOwnedItems_shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}/owned-items", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: 9999"));
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-items - Should return empty list when user owns no items")
    void getUserOwnedItems_shouldReturnEmptyListWhenNoOwnedItems() throws Exception {
        mockMvc.perform(get("/api/users/{id}/owned-items", renter.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

