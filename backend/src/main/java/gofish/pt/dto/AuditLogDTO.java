package gofish.pt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long adminId;
    private String adminUsername;
    private String action;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String details;
    private LocalDateTime createdAt;
}
