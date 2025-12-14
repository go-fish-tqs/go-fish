package gofish.pt.service;

import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.entity.User;
import gofish.pt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserBookings_returnsBookings() {
        Long userId = 1L;
        User user = mock(User.class);
        Booking booking = mock(Booking.class);
        List<Booking> bookings = List.of(booking);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getBookings()).thenReturn(bookings);

        List<Booking> result = userService.getUserBookings(userId);

        assertSame(bookings, result);
        verify(userRepository).findById(userId);
        verify(user).getBookings();
    }

    @Test
    void getUserBookings_userNotFound_throws() {
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.getUserBookings(userId));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserOwnedBookings_returnsOwnedBookings() {
        Long userId = 3L;
        User user = mock(User.class);
        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);
        Booking b1 = mock(Booking.class);
        Booking b2 = mock(Booking.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getItems()).thenReturn(List.of(item1, item2));
        when(item1.getBookings()).thenReturn(List.of(b1));
        when(item2.getBookings()).thenReturn(List.of(b2));

        List<Booking> result = userService.getUserOwnedBookings(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(b1));
        assertTrue(result.contains(b2));
        verify(userRepository).findById(userId);
        verify(user).getItems();
        verify(item1).getBookings();
        verify(item2).getBookings();
    }

    @Test
    void getUserOwnedItems_returnsItems() {
        Long userId = 4L;
        User user = mock(User.class);
        Item item = mock(Item.class);
        List<Item> items = List.of(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getItems()).thenReturn(items);

        List<Item> result = userService.getUserOwnedItems(userId);

        assertSame(items, result);
        verify(userRepository).findById(userId);
        verify(user).getItems();
    }

    @Test
    void getUserOwnedItems_userNotFound_throws() {
        Long userId = 5L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserOwnedItems(userId));
        verify(userRepository).findById(userId);
    }
}
