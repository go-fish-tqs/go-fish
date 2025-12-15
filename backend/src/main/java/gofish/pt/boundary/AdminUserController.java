package gofish.pt.boundary;

import gofish.pt.dto.AdminUserDTO;
import gofish.pt.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    /**
     * GET /api/admin/users - List all users
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * PUT /api/admin/users/{id}/suspend - Suspend a user
     */
    @PutMapping("/{id}/suspend")
    public ResponseEntity<String> suspendUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        Long adminId = parseAdminId(authentication);
        String reason = body != null ? body.get("reason") : "Suspended by admin";
        adminService.suspendUser(id, adminId, reason);
        return ResponseEntity.ok("User suspended successfully");
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = parseAdminId(authentication);
        adminService.reactivateUser(id, adminId);
        return ResponseEntity.ok("User reactivated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        Long adminId = parseAdminId(authentication);
        String reason = body != null ? body.get("reason") : "Deleted by admin";
        adminService.softDeleteUser(id, adminId, reason);
        return ResponseEntity.ok("User deleted successfully");
    }

    private Long parseAdminId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }
        throw new IllegalArgumentException("Invalid principal type");
    }
}
