package gofish.pt.integration;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.config.TestSecurityConfig;
import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingStatusDTO;
import gofish.pt.entity.*;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository;
import gofish.pt.security.TestSecurityContextHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class BookingControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private BookingRepository bookingRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;

    private User owner;
    private User renter;
    private Item kayak;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Criar Owner
        owner = new User();
        owner.setUsername("dono_kayak");
        owner.setEmail("dono@rio.pt");
        owner.setPassword("pass");
        owner.setLocation("Tavira");
        owner.setBalance(0.0);
        owner = userRepository.saveAndFlush(owner);

        // 2. Criar Renter (Cliente)
        renter = new User();
        renter.setUsername("cliente_turista");
        renter.setEmail("tourist@uk.co");
        renter.setPassword("pass");
        renter.setLocation("Hotel");
        renter.setBalance(100.0);
        renter = userRepository.saveAndFlush(renter);

        // 3. Criar Item
        kayak = new Item();
        kayak.setName("Kayak Duplo");
        kayak.setDescription("Para passear na ria");
        kayak.setPrice(25.0);
        kayak.setCategory(Category.BOATS);
        kayak.setMaterial(Material.ROTOMOLDED_POLYETHYLENE);
        kayak.setOwner(owner);
        kayak.setAvailable(true);
        kayak = itemRepository.saveAndFlush(kayak);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContextHelper.clearContext();
    }

    @Test
    @DisplayName("POST /api/bookings - Deve criar reserva PENDING")
    @Requirement("GF-48")
    void createBooking() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(renter.getId());

        // Arrange
        BookingRequestDTO request = new BookingRequestDTO();
        request.setItemId(kayak.getId());
        // Datas futuras para não dar erro
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        // AUTENTICAÇÃO: Simula o Renter
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.itemName").value("Kayak Duplo"));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Deve devolver erro 404 se não existir")
    @Requirement("GF-95")
    void getBookingNotFound() throws Exception {
        mockMvc.perform(get("/api/bookings/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status - Dono aprova reserva")
    @Requirement("GF-48")
    void updateBookingStatus() throws Exception {
        // 1. Criar uma reserva PENDING
        Booking booking = new Booking();
        booking.setUser(renter);
        booking.setItem(kayak);
        booking.setStartDate(LocalDate.now());
        booking.setEndDate(LocalDate.now());
        booking.setStatus(BookingStatus.PENDING);
        booking = bookingRepository.saveAndFlush(booking);

        TestSecurityContextHelper.setAuthenticatedUser(owner.getId()); // Autenticar como dono

        // 2. Preparar DTO de aprovação
        BookingStatusDTO statusDTO = new BookingStatusDTO();
        statusDTO.setStatus(BookingStatus.CONFIRMED);

        // Act & Assert
        mockMvc.perform(patch("/api/bookings/{id}/status", booking.getId())
                        // AUTENTICAÇÃO: O DONO é quem aprova
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status - Erro 403 se não for o dono")
    @Requirement("GF-48")
    void updateBookingStatus_SecurityCheck() throws Exception {
        // 1. Criar reserva
        Booking booking = new Booking();
        booking.setUser(renter);
        booking.setItem(kayak);
        booking.setStartDate(LocalDate.now());
        booking.setEndDate(LocalDate.now());
        booking.setStatus(BookingStatus.PENDING);
        booking = bookingRepository.saveAndFlush(booking);

        TestSecurityContextHelper.setAuthenticatedUser(renter.getId()); // ID errado (não é o dono do item)

        // 2. O Renter tenta aprovar a própria reserva (Espertinho!)
        BookingStatusDTO statusDTO = new BookingStatusDTO();
        statusDTO.setStatus(BookingStatus.CONFIRMED);

        // Act & Assert
        mockMvc.perform(patch("/api/bookings/{id}/status", booking.getId())
                        // AUTENTICAÇÃO: Logado como RENTER (não é o dono do item)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                        .andExpect(status().isForbidden()); // Espera-se 403 Forbidden
    }
}