package com.modeon.backend.payment.controller;

import com.modeon.backend.entity.User;
import com.modeon.backend.payment.dto.PaymentConfirmRequest;
import com.modeon.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
            @AuthenticationPrincipal User user,
            @RequestBody PaymentConfirmRequest request
    ) {
        paymentService.confirmPayment(user, request);
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }
}
