package com.payflow.controller;

import com.payflow.dto.MerchantRequest;
import com.payflow.dto.MerchantResponse;
import com.payflow.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody MerchantRequest request) {
        log.info("POST /api/v1/merchants - Creating merchant");
        MerchantResponse response = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> getMerchant(@PathVariable String merchantId) {
        log.info("GET /api/v1/merchants/{} - Fetching merchant", merchantId);
        MerchantResponse response = merchantService.getMerchant(merchantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MerchantResponse>> getAllMerchants() {
        log.info("GET /api/v1/merchants - Fetching all merchants");
        List<MerchantResponse> response = merchantService.getAllMerchants();
        return ResponseEntity.ok(response);
    }
}