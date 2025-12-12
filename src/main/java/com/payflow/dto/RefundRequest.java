package com.payflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal amount;  // If null, full refund

    private String reason;
}