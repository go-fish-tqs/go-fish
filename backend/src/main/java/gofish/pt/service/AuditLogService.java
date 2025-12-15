package gofish.pt.service;

import gofish.pt.entity.AuditLog;
import gofish.pt.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an admin action
     */
    public AuditLog log(Long adminId, String action, String targetType, Long targetId, String details) {
        AuditLog auditLog = new AuditLog(adminId, action, targetType, targetId, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Get all audit logs ordered by creation date
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get audit logs with optional filters
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsWithFilters(String action, LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findWithFilters(action, from, to);
    }

    /**
     * Get audit logs for a specific target
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForTarget(String targetType, Long targetId) {
        return auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
    }
}
