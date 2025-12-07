package gofish.pt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.boundary.BookingController;
import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.dto.BookingStatusDTO; // <--- Confirma se tens este DTO criado!
import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.mapper.BookingMapper;
import gofish.pt.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class) // Carrega só o Controller
class BookingControllerIT {

    @Autowired
    private MockMvc mockMvc; // O carteiro que manda os pedidos HTTP a fingir

    @Autowired
    private ObjectMapper objectMapper; // Para converter Objetos em JSON

    @MockitoBean
    private BookingService bookingService; // O Serviço a fingir

    @MockitoBean
    private BookingMapper bookingMapper; // O Mapper a fingir

    private BookingRequestDTO requestDTO;
    private BookingResponseDTO responseDTO;
    private Booking bookingEntity;

    @BeforeEach
    void setup() {
        // Preparar dados de exemplo
        requestDTO = new BookingRequestDTO();
        requestDTO.setUserId(10L);
        requestDTO.setItemId(5L);
        requestDTO.setStartDate(LocalDateTime.now().plusDays(1));
        requestDTO.setEndDate(LocalDateTime.now().plusDays(3));

        bookingEntity = new Booking();
        bookingEntity.setId(1L);
        bookingEntity.setStatus(BookingStatus.PENDING);

        responseDTO = new BookingResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(BookingStatus.PENDING);
        responseDTO.setUserId(10L);
        responseDTO.setItemId(5L);
    }

    // --- TESTES DE POST (Criar Reserva) ---

    @Test
    @DisplayName("POST /api/bookings - Deve criar reserva e devolver 201 Created")
    void shouldCreateBooking_WhenValid() throws Exception {
        // Arrange
        // Ensinar o Serviço Mock a devolver a entidade quando chamado
        when(bookingService.createBooking(any(), any(), any(), any()))
                .thenReturn(bookingEntity);

        // Ensinar o Mapper Mock a devolver o DTO de resposta
        when(bookingMapper.toDTO(any(Booking.class)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))) // Converte DTO para JSON string
                .andExpect(status().isCreated()) // Espera HTTP 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/bookings - Deve dar erro 400 se faltar dados")
    void shouldReturn400_WhenInvalidBody() throws Exception {
        // Arrange
        BookingRequestDTO invalidRequest = new BookingRequestDTO();
        // Não preenchemos nada (tudo null), o @Valid deve apanhar isto

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Espera HTTP 400
    }

    // --- TESTES DE GET (Buscar Reserva) ---

    @Test
    @DisplayName("GET /api/bookings/{id} - Deve devolver a reserva se existir")
    void shouldGetBooking_WhenExists() throws Exception {
        // Arrange
        when(bookingService.getBooking(1L)).thenReturn(Optional.of(bookingEntity));
        when(bookingMapper.toDTO(bookingEntity)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Deve dar 404 Not Found se não existir")
    void shouldFail_WhenBookingNotFound() throws Exception {
        // Arrange
        when(bookingService.getBooking(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound()); // <--- Agora esperamos 404, não 500
    }

    // --- TESTES DE PATCH (Atualizar Status) ---

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status - Deve atualizar o estado")
    void shouldUpdateStatus() throws Exception {
        // Arrange
        BookingStatusDTO statusUpdate = new BookingStatusDTO();
        statusUpdate.setStatus(BookingStatus.CONFIRMED);
        statusUpdate.setOwnerId(20L);

        Booking confirmedBooking = new Booking();
        confirmedBooking.setId(1L);
        confirmedBooking.setStatus(BookingStatus.CONFIRMED);

        BookingResponseDTO confirmedResponse = new BookingResponseDTO();
        confirmedResponse.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.updateBookingStatus(eq(1L), eq(BookingStatus.CONFIRMED), eq(20L)))
                .thenReturn(confirmedBooking);
        when(bookingMapper.toDTO(confirmedBooking)).thenReturn(confirmedResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/bookings/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}