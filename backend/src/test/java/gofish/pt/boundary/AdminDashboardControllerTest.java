package gofish.pt.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import gofish.pt.dto.AdminDashboardDTO;
import gofish.pt.entity.AuditLog;
import gofish.pt.entity.Booking;
import gofish.pt.entity.User;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.UserRepository;
import gofish.pt.service.AdminService;
import gofish.pt.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private UserRepository userRepository;

    private AdminDashboardDTO dashboardDTO;
    private AuditLog testAuditLog;
    private User adminUser;

    @BeforeEach
    void setUp() {
        dashboardDTO = new AdminDashboardDTO(5, 3, 100, 2, 50, 5, 15000.0);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");

        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setAdminId(1L);
        testAuditLog.setAction("SUSPEND_USER");
        testAuditLog.setTargetType("USER");
        testAuditLog.setTargetId(10L);
        testAuditLog.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/admin/dashboard - Should return dashboard stats")
    void getDashboard_returnsStats() throws Exception {
        when(adminService.getDashboardStats()).thenReturn(dashboardDTO);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeBookings").value(5))
                .andExpect(jsonPath("$.pendingBookings").value(3))
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.suspendedUsers").value(2))
                .andExpect(jsonPath("$.totalItems").value(50))
                .andExpect(jsonPath("$.inactiveItems").value(5))
                .andExpect(jsonPath("$.totalRevenue").value(15000.0));

        verify(adminService).getDashboardStats();
    }

    @Test
    @DisplayName("GET /api/admin/bookings - Should return all bookings")
    void getAllBookings_returnsBookings() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingRepository).findAll();
    }

    @Test
    @DisplayName("GET /api/admin/audit - Should return audit logs")
    void getAuditLogs_returnsLogs() throws Exception {
        when(auditLogService.getAllLogs()).thenReturn(List.of(testAuditLog));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SUSPEND_USER"))
                .andExpect(jsonPath("$[0].adminUsername").value("admin"));

        verify(auditLogService).getAllLogs();
    }

    @Test
    @DisplayName("GET /api/admin/audit - Should filter by action")
    void getAuditLogs_withActionFilter_returnsFilteredLogs() throws Exception {
        when(auditLogService.getLogsWithFilters(eq("SUSPEND_USER"), any(), any()))
                .thenReturn(List.of(testAuditLog));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/audit")
                .param("action", "SUSPEND_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SUSPEND_USER"));

        verify(auditLogService).getLogsWithFilters(eq("SUSPEND_USER"), any(), any());
    }
}
