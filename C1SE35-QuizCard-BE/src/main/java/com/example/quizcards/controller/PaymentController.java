package com.example.quizcards.controller;

import com.example.quizcards.dto.request.PaymentRequest;
import com.example.quizcards.service.impl.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            String result = paymentService.processPayment(paymentRequest);
            return ResponseEntity.ok(result);
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Payment processing failed: " + e.getMessage());
        }
    }
}
