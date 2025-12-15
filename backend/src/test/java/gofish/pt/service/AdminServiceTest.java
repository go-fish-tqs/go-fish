package gofish.pt.service;

import gofish.pt.dto.AdminDashboardDTO;
import gofish.pt.dto.AdminUserDTO;
import gofish.pt.entity.*;
import gofish.pt.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminService adminService;

    private User regularUser;
    private User adminUser;
    private UserRole adminRole;
    private UserRole userRole;
    private UserStatus activeStatus;
    private Item item;

    @BeforeEach
    void setUp() {
        regularUser = new User();
        regularUser.setId(10L);
        regularUser.setUsername("regular_user");
        regularUser.setEmail("user@test.com");
        regularUser.setLocation("Test Location");
        regularUser.setItems(new ArrayList<>());
        regularUser.setBookings(new ArrayList<>());

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@gofish.pt");

        userRole = new UserRole(10L, UserRole.ROLE_USER);
        adminRole = new UserRole(1L, UserRole.ROLE_ADMIN);
        activeStatus = new UserStatus(10L, UserStatus.STATUS_ACTIVE);

        item = new Item();
        item.setId(100L);
        item.setName("Test Fishing Rod");
        item.setActive(true);
        item.setOwner(regularUser);
    }

    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {

        @Test
        @DisplayName("Should return all users with their role and status")
        void shouldGetAllUsers() {
            // Arrange
            when(userRepository.findAll()).thenReturn(List.of(regularUser, adminUser));
            when(userRoleRepository.findByUserId(10L)).thenReturn(Optional.of(userRole));
            when(userRoleRepository.findByUserId(1L)).thenReturn(Optional.of(adminRole));
            when(userStatusRepository.findByUserId(anyLong())).thenReturn(Optional.of(activeStatus));

            // Act
            List<AdminUserDTO> result = adminService.getAllUsers();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUsername()).isEqualTo("regular_user");
            assertThat(result.get(0).getRole()).isEqualTo(UserRole.ROLE_USER);
        }

        @Test
        @DisplayName("Should suspend regular user successfully")
        void shouldSuspendUser() {
            // Arrange
            when(userRepository.findById(10L)).thenReturn(Optional.of(regularUser));
            when(userRoleRepository.findByUserId(10L)).thenReturn(Optional.of(userRole));
            when(userStatusRepository.findByUserId(10L)).thenReturn(Optional.of(activeStatus));
            when(userStatusRepository.save(any(UserStatus.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminService.suspendUser(10L, 1L, "Terms violation");

            // Assert
            assertThat(activeStatus.getStatus()).isEqualTo(UserStatus.STATUS_SUSPENDED);
            assertThat(activeStatus.getReason()).isEqualTo("Terms violation");
            verify(auditLogService).log(eq(1L), eq(AuditLog.ACTION_SUSPEND_USER), eq(AuditLog.TARGET_USER), eq(10L),
                    anyString());
        }

        @Test
        @DisplayName("Should throw error when trying to suspend admin user")
        void shouldThrowWhenSuspendingAdmin() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(userRoleRepository.findByUserId(1L)).thenReturn(Optional.of(adminRole));

            // Act & Assert
            assertThatThrownBy(() -> adminService.suspendUser(1L, 2L, "Test"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot suspend admin");
        }

        @Test
        @DisplayName("Should reactivate suspended user")
        void shouldReactivateUser() {
            // Arrange
            UserStatus suspendedStatus = new UserStatus(10L, UserStatus.STATUS_SUSPENDED);
            suspendedStatus.setReason("Previous violation");

            when(userRepository.findById(10L)).thenReturn(Optional.of(regularUser));
            when(userStatusRepository.findByUserId(10L)).thenReturn(Optional.of(suspendedStatus));
            when(userStatusRepository.save(any(UserStatus.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminService.reactivateUser(10L, 1L);

            // Assert
            assertThat(suspendedStatus.getStatus()).isEqualTo(UserStatus.STATUS_ACTIVE);
            assertThat(suspendedStatus.getReason()).isNull();
            verify(auditLogService).log(1L, AuditLog.ACTION_REACTIVATE_USER, AuditLog.TARGET_USER, 10L, null);
        }

        @Test
        @DisplayName("Should soft-delete user")
        void shouldSoftDeleteUser() {
            // Arrange
            when(userRepository.findById(10L)).thenReturn(Optional.of(regularUser));
            when(userRoleRepository.findByUserId(10L)).thenReturn(Optional.of(userRole));
            when(userStatusRepository.findByUserId(10L)).thenReturn(Optional.of(activeStatus));
            when(userStatusRepository.save(any(UserStatus.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminService.softDeleteUser(10L, 1L, "Account closure requested");

            // Assert
            assertThat(activeStatus.getStatus()).isEqualTo(UserStatus.STATUS_DELETED);
            assertThat(activeStatus.getReason()).isEqualTo("Account closure requested");
            verify(auditLogService).log(eq(1L), eq(AuditLog.ACTION_DELETE_USER), eq(AuditLog.TARGET_USER), eq(10L),
                    anyString());
        }

        @Test
        @DisplayName("Should throw error when user not found")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> adminService.suspendUser(999L, 1L, "Test"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Item Management Tests")
    class ItemManagementTests {

        @Test
        @DisplayName("Should return all items including inactive")
        void shouldGetAllItems() {
            // Arrange
            Item inactiveItem = new Item();
            inactiveItem.setId(101L);
            inactiveItem.setActive(false);

            when(itemRepository.findAll()).thenReturn(List.of(item, inactiveItem));

            // Act
            List<Item> result = adminService.getAllItems();

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should deactivate item with reason")
        void shouldDeactivateItem() {
            // Arrange
            when(itemRepository.findById(100L)).thenReturn(Optional.of(item));
            when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminService.deactivateItem(100L, "Violates policy", 1L);

            // Assert
            assertThat(item.getActive()).isFalse();
            assertThat(item.getDeactivationReason()).isEqualTo("Violates policy");
            verify(auditLogService).log(eq(1L), eq(AuditLog.ACTION_DEACTIVATE_ITEM), eq(AuditLog.TARGET_ITEM), eq(100L),
                    anyString());
        }

        @Test
        @DisplayName("Should reactivate item")
        void shouldReactivateItem() {
            // Arrange
            item.setActive(false);
            item.setDeactivationReason("Previous issue");

            when(itemRepository.findById(100L)).thenReturn(Optional.of(item));
            when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminService.reactivateItem(100L, 1L);

            // Assert
            assertThat(item.getActive()).isTrue();
            assertThat(item.getDeactivationReason()).isNull();
            verify(auditLogService).log(1L, AuditLog.ACTION_REACTIVATE_ITEM, AuditLog.TARGET_ITEM, 100L, null);
        }

        @Test
        @DisplayName("Should throw error when item not found")
        void shouldThrowWhenItemNotFound() {
            // Arrange
            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> adminService.deactivateItem(999L, "Test", 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Item not found");
        }
    }

    @Nested
    @DisplayName("Dashboard Tests")
    class DashboardTests {

        @Test
        @DisplayName("Should calculate dashboard statistics correctly")
        void shouldGetDashboardStats() {
            // Arrange
            when(userRepository.count()).thenReturn(10L);
            when(userStatusRepository.findAllByStatus(UserStatus.STATUS_SUSPENDED))
                    .thenReturn(List.of(new UserStatus(5L, UserStatus.STATUS_SUSPENDED)));

            Item activeItem = new Item();
            activeItem.setActive(true);
            Item inactiveItem = new Item();
            inactiveItem.setActive(false);
            when(itemRepository.findAll()).thenReturn(List.of(activeItem, inactiveItem));

            Booking confirmedBooking = new Booking();
            confirmedBooking.setStatus(BookingStatus.CONFIRMED);
            confirmedBooking.setPrice(100.0);
            Booking pendingBooking = new Booking();
            pendingBooking.setStatus(BookingStatus.PENDING);
            when(bookingRepository.findAll()).thenReturn(List.of(confirmedBooking, pendingBooking));

            // Act
            AdminDashboardDTO result = adminService.getDashboardStats();

            // Assert
            assertThat(result.getTotalUsers()).isEqualTo(10);
            assertThat(result.getSuspendedUsers()).isEqualTo(1);
            assertThat(result.getTotalItems()).isEqualTo(2);
            assertThat(result.getInactiveItems()).isEqualTo(1);
            assertThat(result.getActiveBookings()).isEqualTo(1);
            assertThat(result.getPendingBookings()).isEqualTo(1);
            assertThat(result.getTotalRevenue()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should handle empty data for dashboard")
        void shouldHandleEmptyDashboard() {
            // Arrange
            when(userRepository.count()).thenReturn(0L);
            when(userStatusRepository.findAllByStatus(anyString())).thenReturn(List.of());
            when(itemRepository.findAll()).thenReturn(List.of());
            when(bookingRepository.findAll()).thenReturn(List.of());

            // Act
            AdminDashboardDTO result = adminService.getDashboardStats();

            // Assert
            assertThat(result.getTotalUsers()).isEqualTo(0);
            assertThat(result.getTotalRevenue()).isEqualTo(0.0);
        }
    }
}
