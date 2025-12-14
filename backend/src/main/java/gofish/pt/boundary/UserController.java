package gofish.pt.boundary;

import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{id}/bookings")
    public List<Booking> getUserBookings(@PathVariable Long id) {
        return userService.getUserBookings(id);
    }

    @GetMapping("{id}/owned-bookings")
    public List<Booking> getUserOwnedBookings(@PathVariable Long id) {
        return userService.getUserOwnedBookings(id);
    }

    @GetMapping("{id}/owned-items")
    public List<Item> getUserOwnedItems(@PathVariable Long id) {
        return userService.getUserOwnedItems(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleUserNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
