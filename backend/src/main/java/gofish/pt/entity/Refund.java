package gofish.pt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double requestedAmount;

    private Double approvedAmount;

    @Column(length = 1024)
    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
}
