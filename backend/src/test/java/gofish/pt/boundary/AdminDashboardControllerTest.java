package gofish.pt.boundary;

import gofish.pt.dto.AdminDashboardDTO;
import gofish.pt.entity.AuditLog;
import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.UserRepository;
import gofish.pt.service.AdminService;
import gofish.pt.service.AuditLogService;
import gofish.pt.config.SecurityConfig;
import gofish.pt.security.JwtAuthenticationFilter;
import gofish.pt.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@Import(SecurityConfig.class)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should return 403 when non-admin tries to access dashboard")
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return dashboard stats for admin")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnDashboardStats() throws Exception {
        // Arrange
        AdminDashboardDTO stats = new AdminDashboardDTO();
        stats.setActiveBookings(5);
        stats.setPendingBookings(3);
        stats.setTotalUsers(100);
        stats.setSuspendedUsers(2);
        stats.setTotalItems(50);
        stats.setInactiveItems(5);
        stats.setTotalRevenue(1500.0);
        when(adminService.getDashboardStats()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.activeBookings").value(5))
                .andExpect(jsonPath("$.totalRevenue").value(1500.0));
    }

    @Test
    @DisplayName("Should return all bookings for admin")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAllBookings() throws Exception {
        // Arrange
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        // Act & Assert
        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Should return audit logs for admin")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAuditLogs() throws Exception {
        // Arrange
        AuditLog log = new AuditLog(1L, AuditLog.ACTION_SUSPEND_USER, AuditLog.TARGET_USER, 10L, null);
        log.setId(100L);
        when(auditLogService.getAllLogs()).thenReturn(List.of(log));

        // Act & Assert
        mockMvc.perform(get("/api/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value(AuditLog.ACTION_SUSPEND_USER));
    }

    @Test
    @DisplayName("Should filter audit logs by action")
    @WithMockUser(roles = "ADMIN")
    void shouldFilterAuditLogsByAction() throws Exception {
        // Arrange
        AuditLog log = new AuditLog(1L, AuditLog.ACTION_SUSPEND_USER, AuditLog.TARGET_USER, 10L, null);
        when(auditLogService.getLogsWithFilters(eq(AuditLog.ACTION_SUSPEND_USER), any(), any()))
                .thenReturn(List.of(log));

        // Act & Assert
        mockMvc.perform(get("/api/admin/audit")
                .param("action", AuditLog.ACTION_SUSPEND_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value(AuditLog.ACTION_SUSPEND_USER));
    }
}
