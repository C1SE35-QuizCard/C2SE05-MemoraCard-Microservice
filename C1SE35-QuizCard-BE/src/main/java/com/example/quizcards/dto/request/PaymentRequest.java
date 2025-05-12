package com.example.quizcards.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentMethodId;
    private Long userId;
    private BigDecimal amount;
    private String planType;
    private String subcription;
}