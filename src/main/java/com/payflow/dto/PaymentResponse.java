package com.payflow.dto;

import com.payflow.statemachine.PaymentState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private PaymentState status;
    private String cardNumberMasked;  // e.g., "**** **** **** 1234"
    private String description;
    private BigDecimal refundedAmount;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}