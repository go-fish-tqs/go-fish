package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gofish.pt.dto.BookingRequestDTO;
import gofish.pt.dto.BookingResponseDTO;
import gofish.pt.dto.BookingStatusDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.mapper.BookingMapper;
import gofish.pt.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private BookingMapper bookingMapper;

    private Booking testBooking;
    private BookingResponseDTO testResponseDTO;
    private User testUser;
    private Item testItem;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(25.0);

        startDate = LocalDateTime.now().plusDays(1);
        endDate = LocalDateTime.now().plusDays(3);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setStartDate(startDate);
        testBooking.setEndDate(endDate);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setUser(testUser);
        testBooking.setItem(testItem);

        testResponseDTO = new BookingResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setStartDate(startDate);
        testResponseDTO.setEndDate(endDate);
        testResponseDTO.setStatus(BookingStatus.PENDING);
        testResponseDTO.setUserId(1L);
        testResponseDTO.setUserName("testuser");
        testResponseDTO.setItemId(1L);
        testResponseDTO.setItemName("Test Item");
        testResponseDTO.setPrice(50.0);
    }

    @Test
    @DisplayName("POST /api/bookings - Should create booking and return 201")
    void createBooking_withValidRequest_returnsCreated() throws Exception {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserId(1L);
        request.setItemId(1L);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        when(bookingService.createBooking(eq(1L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(bookingService).createBooking(eq(1L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingMapper).toDTO(testBooking);
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Should return booking when found")
    void getBooking_whenExists_returnsBooking() throws Exception {
        when(bookingService.getBooking(1L)).thenReturn(Optional.of(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/bookings/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemName").value("Test Item"));

        verify(bookingService).getBooking(1L);
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Should return 404 when not found")
    void getBooking_whenNotExists_returns404() throws Exception {
        when(bookingService.getBooking(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/{id}", 999))
                .andExpect(status().isNotFound());

        verify(bookingService).getBooking(999L);
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status - Should update booking status")
    void updateStatus_withValidRequest_returnsUpdatedBooking() throws Exception {
        BookingStatusDTO statusDto = new BookingStatusDTO();
        statusDto.setStatus(BookingStatus.CONFIRMED);
        statusDto.setOwnerId(1L);

        testBooking.setStatus(BookingStatus.CONFIRMED);
        testResponseDTO.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED, 1L)).thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(patch("/api/bookings/{id}/status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).updateBookingStatus(1L, BookingStatus.CONFIRMED, 1L);
    }

    @Test
    @DisplayName("GET /api/bookings/item/{itemId}/month - Should return bookings for month")
    void getBookingsByMonth_returnsBookingsList() throws Exception {
        when(bookingService.getBookingsByItemAndMonth(1L, 2024, 12)).thenReturn(List.of(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/bookings/item/{itemId}/month", 1)
                .param("year", "2024")
                .param("month", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getBookingsByItemAndMonth(1L, 2024, 12);
    }

    @Test
    @DisplayName("GET /api/bookings/item/{itemId}/week - Should return bookings for week")
    void getBookingsByWeek_returnsBookingsList() throws Exception {
        LocalDate weekStart = LocalDate.of(2024, 12, 9);

        when(bookingService.getBookingsByItemAndWeek(1L, weekStart)).thenReturn(List.of(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/bookings/item/{itemId}/week", 1)
                .param("start", "2024-12-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getBookingsByItemAndWeek(1L, weekStart);
    }

    @Test
    @DisplayName("GET /api/bookings/item/{itemId}/month - Should return empty list when no bookings")
    void getBookingsByMonth_whenNoBookings_returnsEmptyList() throws Exception {
        when(bookingService.getBookingsByItemAndMonth(1L, 2024, 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/item/{itemId}/month", 1)
                .param("year", "2024")
                .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookingService).getBookingsByItemAndMonth(1L, 2024, 1);
    }
}
