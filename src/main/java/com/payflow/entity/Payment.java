package com.payflow.entity;

import com.payflow.statemachine.PaymentState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId;  // Public transaction ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentState status;

    // Simulated card details (in production, these would be tokenized)
    @Column(nullable = false)
    private String cardNumber;  // Last 4 digits only

    @Column(nullable = false)
    private String cardHolderName;

    private String description;

    @Column(nullable = false)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = PaymentState.PENDING;
        if (refundedAmount == null) {
            refundedAmount = BigDecimal.ZERO;
        }
    }
}