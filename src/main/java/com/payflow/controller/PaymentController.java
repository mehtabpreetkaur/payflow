package com.payflow.controller;

import com.payflow.dto.PaymentRequest;
import com.payflow.dto.PaymentResponse;
import com.payflow.dto.RefundRequest;
import com.payflow.entity.Merchant;
import com.payflow.service.MerchantService;
import com.payflow.service.PaymentService;
import com.payflow.statemachine.PaymentState;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/v1/payments - Creating payment");
        Merchant merchant = merchantService.validateApiKey(apiKey);
        PaymentResponse response = paymentService.createPayment(merchant, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{transactionId}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String transactionId) {
        log.info("POST /api/v1/payments/{}/process - Processing payment", transactionId);
        PaymentResponse response = paymentService.processPayment(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@Valid @RequestBody RefundRequest request) {
        log.info("POST /api/v1/payments/refund - Refunding payment");
        PaymentResponse response = paymentService.refundPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String transactionId) {
        log.info("GET /api/v1/payments/{} - Fetching payment", transactionId);
        PaymentResponse response = paymentService.getPayment(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<PaymentResponse>> getMerchantPayments(@PathVariable Long merchantId) {
        log.info("GET /api/v1/payments/merchant/{} - Fetching merchant payments", merchantId);
        List<PaymentResponse> response = paymentService.getMerchantPayments(merchantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentState status) {
        log.info("GET /api/v1/payments/status/{} - Fetching payments by status", status);
        List<PaymentResponse> response = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(response);
    }
}