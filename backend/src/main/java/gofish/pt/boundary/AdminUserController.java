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
    public ResponseEntity<Void> suspendUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = body != null ? body.get("reason") : "Suspended by admin";
        adminService.suspendUser(id, adminId, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/admin/users/{id}/reactivate - Reactivate a suspended user
     */
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        adminService.reactivateUser(id, adminId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/admin/users/{id} - Soft-delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = body != null ? body.get("reason") : "Deleted by admin";
        adminService.softDeleteUser(id, adminId, reason);
        return ResponseEntity.ok().build();
    }
}
