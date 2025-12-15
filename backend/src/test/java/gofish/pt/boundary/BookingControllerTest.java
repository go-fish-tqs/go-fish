package gofish.pt.boundary;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
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
import gofish.pt.security.TestSecurityContextHelper;
import gofish.pt.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit tests
@ActiveProfiles("test") // Activate test profile to exclude production security config
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
    private LocalDate startDate;
    private LocalDate endDate;

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

        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(3);

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

    @AfterEach
    void tearDown() {
        TestSecurityContextHelper.clearContext();
    }

    @Test
    @DisplayName("POST /api/bookings - Should create booking and return 201")
    @Requirement("GF-48")
    void createBooking_withValidRequest_returnsCreated() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        BookingRequestDTO request = new BookingRequestDTO();
        request.setItemId(1L);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        when(bookingService.createBooking(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(bookingService).createBooking(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class));
        verify(bookingMapper).toDTO(testBooking);
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Should return booking when found")
    @Requirement("GF-95")
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
    @Requirement("GF-95")
    void getBooking_whenNotExists_returns404() throws Exception {
        when(bookingService.getBooking(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/{id}", 999))
                .andExpect(status().isNotFound());

        verify(bookingService).getBooking(999L);
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status - Should update booking status")
    @Requirement("GF-48")
    void updateStatus_withValidRequest_returnsUpdatedBooking() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L); // Mock authenticated as owner

        BookingStatusDTO statusDto = new BookingStatusDTO();
        statusDto.setStatus(BookingStatus.CONFIRMED);

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
    @Requirement("GF-49")
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
    @Requirement("GF-49")
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
    @Requirement("GF-49")
    void getBookingsByMonth_whenNoBookings_returnsEmptyList() throws Exception {
        when(bookingService.getBookingsByItemAndMonth(1L, 2024, 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/item/{itemId}/month", 1)
                .param("year", "2024")
                .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookingService).getBookingsByItemAndMonth(1L, 2024, 1);
    }

    @Test
    @DisplayName("GET /api/bookings/my-items - Should return bookings on user's items")
    @Requirement("GF-50")
    void getBookingsOnMyItems_returnsBookingsList() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        when(bookingService.getBookingsByItemOwnerId(1L)).thenReturn(List.of(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/bookings/my-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getBookingsByItemOwnerId(1L);
    }

    @Test
    @DisplayName("GET /api/bookings/my - Should return user's bookings")
    @Requirement("GF-51")
    void getMyBookings_returnsBookingsList() throws Exception {
        TestSecurityContextHelper.setAuthenticatedUser(1L);

        when(bookingService.getBookingsByUserId(1L)).thenReturn(List.of(testBooking));
        when(bookingMapper.toDTO(testBooking)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/bookings/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getBookingsByUserId(1L);
    }
}
