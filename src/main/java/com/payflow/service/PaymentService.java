package com.payflow.service;

import com.payflow.dto.PaymentRequest;
import com.payflow.dto.PaymentResponse;
import com.payflow.dto.RefundRequest;
import com.payflow.entity.Merchant;
import com.payflow.entity.Payment;
import com.payflow.exception.InvalidStateTransitionException;
import com.payflow.exception.PaymentException;
import com.payflow.repository.PaymentRepository;
import com.payflow.statemachine.PaymentEvent;
import com.payflow.statemachine.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Transactional
    public PaymentResponse createPayment(Merchant merchant, PaymentRequest request) {
        log.info("Creating payment for merchant: {}", merchant.getMerchantId());

        // Generate unique transaction ID
        String transactionId = "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        // Mask card number (store only last 4 digits)
        String maskedCard = "**** **** **** " + request.getCardNumber().substring(12);

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .merchant(merchant)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentState.PENDING)
                .cardNumber(maskedCard)
                .cardHolderName(request.getCardHolderName())
                .description(request.getDescription())
                .refundedAmount(BigDecimal.ZERO)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created with transaction ID: {}", transactionId);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse processPayment(String transactionId) {
        log.info("Processing payment: {}", transactionId);

        Payment payment = getPaymentEntity(transactionId);

        // Transition to PROCESSING
        changePaymentState(payment, PaymentEvent.PROCESS);
        payment.setProcessedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Simulate payment processing (in real system, this would call payment processor)
        boolean success = simulatePaymentProcessing(payment);

        if (success) {
            changePaymentState(payment, PaymentEvent.COMPLETE);
            payment.setCompletedAt(LocalDateTime.now());
            log.info("Payment completed: {}", transactionId);
        } else {
            changePaymentState(payment, PaymentEvent.FAIL);
            payment.setFailureReason("Insufficient funds");
            log.warn("Payment failed: {}", transactionId);
        }

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) {
        log.info("Processing refund for transaction: {}", request.getTransactionId());

        Payment payment = getPaymentEntity(request.getTransactionId());

        // Validate payment can be refunded
        if (payment.getStatus() != PaymentState.COMPLETED &&
                payment.getStatus() != PaymentState.PARTIALLY_REFUNDED) {
            throw new PaymentException("Payment must be in COMPLETED or PARTIALLY_REFUNDED state to refund");
        }

        // Determine refund amount
        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : payment.getAmount();
        BigDecimal alreadyRefunded = payment.getRefundedAmount();
        BigDecimal totalRefunded = alreadyRefunded.add(refundAmount);

        // Validate refund amount
        if (totalRefunded.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount exceeds payment amount");
        }

        // Determine if this will be a full or partial refund BEFORE changing state
        boolean isFullRefund = totalRefunded.compareTo(payment.getAmount()) == 0;

        // Change state FIRST, then update amount
        if (isFullRefund) {
            changePaymentState(payment, PaymentEvent.REFUND);
            payment.setRefundedAmount(totalRefunded);
            log.info("Full refund processed for: {}", request.getTransactionId());
        } else {
            // Only transition to PARTIALLY_REFUNDED if currently COMPLETED
            if (payment.getStatus() == PaymentState.COMPLETED) {
                changePaymentState(payment, PaymentEvent.PARTIAL_REFUND);
            }
            // If already PARTIALLY_REFUNDED, the internal transition keeps it there
            payment.setRefundedAmount(totalRefunded);
            log.info("Partial refund processed for: {}", request.getTransactionId());
        }

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String transactionId) {
        Payment payment = getPaymentEntity(transactionId);
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMerchantPayments(Long merchantId) {
        return paymentRepository.findByMerchantId(merchantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentState status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Payment getPaymentEntity(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + transactionId));
    }

    private void changePaymentState(Payment payment, PaymentEvent event) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine();

        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> {
                    accessor.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(payment.getStatus(), null, null, null)
                    ).block();
                });
        stateMachine.startReactively().block();

        Message<PaymentEvent> message = MessageBuilder.withPayload(event).build();
        boolean accepted = stateMachine.sendEvent(message);

        if (!accepted) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition from %s with event %s", payment.getStatus(), event)
            );
        }
        payment.setStatus(stateMachine.getState().getId());
    }
    private boolean simulatePaymentProcessing(Payment payment) {
        // Simulate processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 80% success rate
        // Fail if card number ends with 0000
        return !payment.getCardNumber().endsWith("0000");
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .cardNumberMasked(payment.getCardNumber())
                .description(payment.getDescription())
                .refundedAmount(payment.getRefundedAmount())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}