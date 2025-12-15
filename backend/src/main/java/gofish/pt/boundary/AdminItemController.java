package gofish.pt.boundary;

import gofish.pt.dto.DeactivateItemDTO;
import gofish.pt.entity.Item;
import gofish.pt.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final AdminService adminService;

    /**
     * GET /api/admin/items - List all items including inactive
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(adminService.getAllItems());
    }

    /**
     * PUT /api/admin/items/{id}/deactivate - Deactivate an item
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateItem(
            @PathVariable Long id,
            @Valid @RequestBody DeactivateItemDTO dto,
            Authentication authentication) {
        Long adminId = parseAdminId(authentication);
        adminService.deactivateItem(id, dto.getReason(), adminId);
        return ResponseEntity.ok("Item deactivated successfully");
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivateItem(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = parseAdminId(authentication);
        adminService.reactivateItem(id, adminId);
        return ResponseEntity.ok("Item reactivated successfully");
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
