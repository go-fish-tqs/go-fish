package gofish.pt.service;

import gofish.pt.dto.AdminDashboardDTO;
import gofish.pt.dto.AdminUserDTO;
import gofish.pt.entity.*;
import gofish.pt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserStatusRepository userStatusRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final AuditLogService auditLogService;

    // ==================== USER MANAGEMENT ====================

    /**
     * Get all users with their role and status information
     */
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Suspend a user account
     */
    public void suspendUser(Long userId, Long adminId, String reason) {
        User user = getUserOrThrow(userId);

        // Cannot suspend admin users
        String role = userRoleRepository.findByUserId(userId)
                .map(UserRole::getRole)
                .orElse(UserRole.ROLE_USER);
        if (UserRole.ROLE_ADMIN.equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot suspend admin users");
        }

        UserStatus status = userStatusRepository.findByUserId(userId)
                .orElse(new UserStatus(userId, UserStatus.STATUS_ACTIVE));

        status.setStatus(UserStatus.STATUS_SUSPENDED);
        status.setReason(reason);
        userStatusRepository.save(status);

        auditLogService.log(adminId, AuditLog.ACTION_SUSPEND_USER, AuditLog.TARGET_USER, userId,
                "{\"reason\":\"" + reason + "\"}");
    }

    /**
     * Reactivate a suspended user account
     */
    public void reactivateUser(Long userId, Long adminId) {
        getUserOrThrow(userId);

        UserStatus status = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User status not found"));

        status.setStatus(UserStatus.STATUS_ACTIVE);
        status.setReason(null);
        userStatusRepository.save(status);

        auditLogService.log(adminId, AuditLog.ACTION_REACTIVATE_USER, AuditLog.TARGET_USER, userId, null);
    }

    /**
     * Soft-delete a user (mark as deleted, don't actually remove)
     */
    public void softDeleteUser(Long userId, Long adminId, String reason) {
        User user = getUserOrThrow(userId);

        // Cannot delete admin users
        String role = userRoleRepository.findByUserId(userId)
                .map(UserRole::getRole)
                .orElse(UserRole.ROLE_USER);
        if (UserRole.ROLE_ADMIN.equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete admin users");
        }

        UserStatus status = userStatusRepository.findByUserId(userId)
                .orElse(new UserStatus(userId, UserStatus.STATUS_ACTIVE));

        status.setStatus(UserStatus.STATUS_DELETED);
        status.setReason(reason);
        userStatusRepository.save(status);

        auditLogService.log(adminId, AuditLog.ACTION_DELETE_USER, AuditLog.TARGET_USER, userId,
                "{\"reason\":\"" + reason + "\"}");
    }

    // ==================== ITEM MANAGEMENT ====================

    /**
     * Get all items including inactive ones
     */
    @Transactional(readOnly = true)
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Deactivate an item
     */
    public void deactivateItem(Long itemId, String reason, Long adminId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        item.setActive(false);
        item.setDeactivationReason(reason);
        itemRepository.save(item);

        auditLogService.log(adminId, AuditLog.ACTION_DEACTIVATE_ITEM, AuditLog.TARGET_ITEM, itemId,
                "{\"reason\":\"" + reason + "\"}");

        // TODO: Notify owner via email (integrate with RabbitMQ/SMTP)
    }

    /**
     * Reactivate an item
     */
    public void reactivateItem(Long itemId, Long adminId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        item.setActive(true);
        item.setDeactivationReason(null);
        itemRepository.save(item);

        auditLogService.log(adminId, AuditLog.ACTION_REACTIVATE_ITEM, AuditLog.TARGET_ITEM, itemId, null);
    }

    // ==================== DASHBOARD ====================

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        int totalUsers = (int) userRepository.count();
        int suspendedUsers = userStatusRepository.findAllByStatus(UserStatus.STATUS_SUSPENDED).size();

        List<Item> allItems = itemRepository.findAll();
        int totalItems = allItems.size();
        int inactiveItems = (int) allItems.stream().filter(item -> !item.getActive()).count();

        List<Booking> allBookings = bookingRepository.findAll();
        int activeBookings = (int) allBookings.stream()
                .filter(b -> BookingStatus.CONFIRMED.equals(b.getStatus()))
                .count();
        int pendingBookings = (int) allBookings.stream()
                .filter(b -> BookingStatus.PENDING.equals(b.getStatus()))
                .count();

        double totalRevenue = allBookings.stream()
                .filter(b -> BookingStatus.CONFIRMED.equals(b.getStatus()) && b.getPrice() != null)
                .mapToDouble(Booking::getPrice)
                .sum();

        return new AdminDashboardDTO(
                activeBookings,
                pendingBookings,
                totalUsers,
                suspendedUsers,
                totalItems,
                inactiveItems,
                totalRevenue);
    }

    // ==================== HELPER METHODS ====================

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        String role = userRoleRepository.findByUserId(user.getId())
                .map(UserRole::getRole)
                .orElse(UserRole.ROLE_USER);

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
        String status = userStatus != null ? userStatus.getStatus() : UserStatus.STATUS_ACTIVE;
        String statusReason = userStatus != null ? userStatus.getReason() : null;

        int itemCount = user.getItems() != null ? user.getItems().size() : 0;
        int bookingCount = user.getBookings() != null ? user.getBookings().size() : 0;

        return new AdminUserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getLocation(),
                role,
                status,
                statusReason,
                itemCount,
                bookingCount);
    }
}
