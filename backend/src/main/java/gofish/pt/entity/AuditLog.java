package gofish.pt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 50)
    private String action; // SUSPEND_USER, REACTIVATE_USER, DELETE_USER, DEACTIVATE_ITEM

    @Column(nullable = false, length = 20)
    private String targetType; // USER, ITEM

    @Column(nullable = false)
    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String details; // JSON with additional info

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(Long adminId, String action, String targetType, Long targetId, String details) {
        this.adminId = adminId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
    }

    // Action constants
    public static final String ACTION_SUSPEND_USER = "SUSPEND_USER";
    public static final String ACTION_REACTIVATE_USER = "REACTIVATE_USER";
    public static final String ACTION_DELETE_USER = "DELETE_USER";
    public static final String ACTION_DEACTIVATE_ITEM = "DEACTIVATE_ITEM";
    public static final String ACTION_REACTIVATE_ITEM = "REACTIVATE_ITEM";

    // Target type constants
    public static final String TARGET_USER = "USER";
    public static final String TARGET_ITEM = "ITEM";
}
