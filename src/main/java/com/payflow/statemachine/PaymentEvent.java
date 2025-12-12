package com.payflow.statemachine;

public enum PaymentEvent {
    PROCESS,        // Start processing payment
    COMPLETE,       // Mark as completed
    FAIL,           // Mark as failed
    REFUND,         // Initiate refund
    PARTIAL_REFUND  // Partial refund
}
