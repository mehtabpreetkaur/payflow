package com.payflow.service;

import com.payflow.dto.MerchantRequest;
import com.payflow.dto.MerchantResponse;
import com.payflow.entity.Merchant;
import com.payflow.exception.MerchantNotFoundException;
import com.payflow.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    @Transactional
    public MerchantResponse createMerchant(MerchantRequest request) {
        log.info("Creating merchant: {}", request.getName());

        // Generate unique merchant ID and API key
        String merchantId = "MERCH_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String apiKey = "sk_" + UUID.randomUUID().toString().replace("-", "");

        Merchant merchant = Merchant.builder()
                .merchantId(merchantId)
                .name(request.getName())
                .email(request.getEmail())
                .apiKey(apiKey)
                .active(true)
                .build();

        merchant = merchantRepository.save(merchant);
        log.info("Merchant created with ID: {}", merchant.getMerchantId());

        return mapToResponse(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found: " + merchantId));
        return mapToResponse(merchant);
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getAllMerchants() {
        return merchantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Merchant validateApiKey(String apiKey) {
        return merchantRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new MerchantNotFoundException("Invalid API key"));
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .merchantId(merchant.getMerchantId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .apiKey(merchant.getApiKey())
                .active(merchant.getActive())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}