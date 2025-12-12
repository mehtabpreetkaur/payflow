package com.payflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {
    private String merchantId;
    private String name;
    private String email;
    private String apiKey;
    private Boolean active;
    private LocalDateTime createdAt;
}