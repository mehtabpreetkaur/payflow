package com.payflow.statemachine;

public enum PaymentState {
    PENDING,        // Payment initiated
    PROCESSING,     // Being processed
    COMPLETED,      // Successfully processed
    FAILED,         // Processing failed
    REFUNDED,       // Payment refunded
    PARTIALLY_REFUNDED  // Partial refund applied
}
