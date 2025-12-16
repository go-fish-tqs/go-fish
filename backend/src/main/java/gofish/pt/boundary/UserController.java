package gofish.pt.boundary;

import gofish.pt.dto.UserResponseDTO;
import gofish.pt.dto.UserUpdateDTO;
import gofish.pt.entity.Booking;
import gofish.pt.entity.Item;
import gofish.pt.exception.DuplicateEmailException;
import gofish.pt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserProfile(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO,
            Authentication authentication) {
        
        Long authenticatedUserId = getCurrentUserId(authentication);
        UserResponseDTO updatedUser = userService.updateUser(id, authenticatedUserId, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        
        // The principal is the userId (Long) set in JwtAuthenticationFilter
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        // Fallback: try to parse as String (UserDetails username)
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid user ID format");
            }
        }
        
        throw new IllegalArgumentException("Unable to get user ID from authentication");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleUserNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
