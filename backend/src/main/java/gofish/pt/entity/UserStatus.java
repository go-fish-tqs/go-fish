package gofish.pt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String status; // "ACTIVE", "SUSPENDED", "DELETED"

    private LocalDateTime updatedAt;

    private String reason; // For suspension/deletion reason

    public UserStatus(Long userId, String status) {
        this.userId = userId;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_DELETED = "DELETED";
}
