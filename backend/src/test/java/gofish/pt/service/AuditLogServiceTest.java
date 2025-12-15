package gofish.pt.service;

import gofish.pt.entity.AuditLog;
import gofish.pt.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new AuditLog(1L, AuditLog.ACTION_SUSPEND_USER, AuditLog.TARGET_USER, 10L, "{\"reason\":\"Test\"}");
        sampleLog.setId(100L);
    }

    @Test
    @DisplayName("Should log admin action and save to repository")
    void shouldLogAction() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        AuditLog result = auditLogService.log(1L, AuditLog.ACTION_SUSPEND_USER, AuditLog.TARGET_USER, 10L,
                "{\"reason\":\"Test\"}");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAdminId()).isEqualTo(1L);
        assertThat(result.getAction()).isEqualTo(AuditLog.ACTION_SUSPEND_USER);
        assertThat(result.getTargetType()).isEqualTo(AuditLog.TARGET_USER);
        assertThat(result.getTargetId()).isEqualTo(10L);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should return all logs ordered by creation date")
    void shouldGetAllLogs() {
        // Arrange
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(sampleLog));

        // Act
        List<AuditLog> result = auditLogService.getAllLogs();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sampleLog);
    }

    @Test
    @DisplayName("Should filter logs by action and date range")
    void shouldGetLogsWithFilters() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(auditLogRepository.findWithFilters(AuditLog.ACTION_SUSPEND_USER, from, to))
                .thenReturn(List.of(sampleLog));

        // Act
        List<AuditLog> result = auditLogService.getLogsWithFilters(AuditLog.ACTION_SUSPEND_USER, from, to);

        // Assert
        assertThat(result).hasSize(1);
        verify(auditLogRepository).findWithFilters(AuditLog.ACTION_SUSPEND_USER, from, to);
    }

    @Test
    @DisplayName("Should get logs for specific target")
    void shouldGetLogsForTarget() {
        // Arrange
        when(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(AuditLog.TARGET_USER, 10L))
                .thenReturn(List.of(sampleLog));

        // Act
        List<AuditLog> result = auditLogService.getLogsForTarget(AuditLog.TARGET_USER, 10L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetId()).isEqualTo(10L);
    }
}
