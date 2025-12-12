package gofish.pt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    private Double platformFee;

    private Double netAmount;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status;

    @Column(length = 1024)
    private String bankAccountDetails;

    private LocalDate estimatedArrival;

    private LocalDateTime processedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
