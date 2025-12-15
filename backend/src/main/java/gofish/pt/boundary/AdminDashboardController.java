package gofish.pt.boundary;

import gofish.pt.dto.AdminDashboardDTO;
import gofish.pt.dto.AuditLogDTO;
import gofish.pt.entity.AuditLog;
import gofish.pt.entity.Booking;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.UserRepository;
import gofish.pt.service.AdminService;
import gofish.pt.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    /**
     * GET /api/admin/dashboard - Get dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /**
     * GET /api/admin/bookings - List all bookings with optional filters
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long itemId) {

        List<Booking> bookings = bookingRepository.findAll();

        // Apply filters
        if (status != null) {
            bookings = bookings.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus().name()))
                    .collect(Collectors.toList());
        }
        if (userId != null) {
            bookings = bookings.stream()
                    .filter(b -> userId.equals(b.getUser().getId()))
                    .collect(Collectors.toList());
        }
        if (itemId != null) {
            bookings = bookings.stream()
                    .filter(b -> itemId.equals(b.getItem().getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/admin/audit - Get audit log entries
     */
    @GetMapping("/audit")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<AuditLog> logs;
        if (action != null || from != null || to != null) {
            logs = auditLogService.getLogsWithFilters(action, from, to);
        } else {
            logs = auditLogService.getAllLogs();
        }

        List<AuditLogDTO> dtos = logs.stream()
                .map(this::toAuditLogDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private AuditLogDTO toAuditLogDTO(AuditLog log) {
        String adminUsername = userRepository.findById(log.getAdminId())
                .map(u -> u.getUsername())
                .orElse("Unknown");

        String targetName = getTargetName(log.getTargetType(), log.getTargetId());

        return new AuditLogDTO(
                log.getId(),
                log.getAdminId(),
                adminUsername,
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                targetName,
                log.getDetails(),
                log.getCreatedAt());
    }

    private String getTargetName(String targetType, Long targetId) {
        if (AuditLog.TARGET_USER.equals(targetType)) {
            return userRepository.findById(targetId)
                    .map(u -> u.getUsername())
                    .orElse("User #" + targetId);
        }
        return "Item #" + targetId;
    }
}
