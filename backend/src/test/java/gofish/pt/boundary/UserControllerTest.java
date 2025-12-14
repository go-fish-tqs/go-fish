package gofish.pt.boundary;

import gofish.pt.config.TestSecurityConfig;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private Booking testBooking;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(1L);

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Rod");
    }

    @Test
    @DisplayName("GET /api/users/{id}/bookings - Should return user bookings")
    void getUserBookings_whenUserExists_returnsBookings() throws Exception {
        Long userId = 1L;
        when(userService.getUserBookings(userId)).thenReturn(List.of(testBooking));

        mockMvc.perform(get("/api/users/{id}/bookings", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(userService).getUserBookings(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id}/bookings - Should return 404 when user not found")
    void getUserBookings_whenUserNotFound_returns404() throws Exception {
        Long userId = 999L;
        when(userService.getUserBookings(userId)).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/users/{id}/bookings", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userService).getUserBookings(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-bookings - Should return owned bookings")
    void getUserOwnedBookings_whenUserExists_returnsOwnedBookings() throws Exception {
        Long userId = 1L;
        when(userService.getUserOwnedBookings(userId)).thenReturn(List.of(testBooking));

        mockMvc.perform(get("/api/users/{id}/owned-bookings", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(userService).getUserOwnedBookings(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-bookings - Should return 404 when user not found")
    void getUserOwnedBookings_whenUserNotFound_returns404() throws Exception {
        Long userId = 999L;
        when(userService.getUserOwnedBookings(userId)).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/users/{id}/owned-bookings", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userService).getUserOwnedBookings(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-items - Should return owned items")
    void getUserOwnedItems_whenUserExists_returnsOwnedItems() throws Exception {
        Long userId = 1L;
        when(userService.getUserOwnedItems(userId)).thenReturn(List.of(testItem));

        mockMvc.perform(get("/api/users/{id}/owned-items", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Rod"));

        verify(userService).getUserOwnedItems(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id}/owned-items - Should return 404 when user not found")
    void getUserOwnedItems_whenUserNotFound_returns404() throws Exception {
        Long userId = 999L;
        when(userService.getUserOwnedItems(userId)).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/users/{id}/owned-items", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userService).getUserOwnedItems(userId);
    }
}
