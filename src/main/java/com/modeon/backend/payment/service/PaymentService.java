package com.modeon.backend.payment.service;

import com.modeon.backend.cart.entity.Cart;
import com.modeon.backend.cart.repository.CartRepository;
import com.modeon.backend.entity.User;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.payment.dto.PaymentConfirmRequest;
import com.modeon.backend.payment.entity.Payment;
import com.modeon.backend.payment.repository.PaymentRepository;
import com.modeon.backend.service.MembershipService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final EntityManager entityManager;
    private final HistoryRepository historyRepository;
    private final MembershipService membershipService;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Transactional
    public void confirmPayment(User user, PaymentConfirmRequest request) {
        // 중복 결제 방지
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new RuntimeException("이미 처리된 주문입니다.");
        }

        // ⭐ 결제 진행 전에 장바구니 먼저 확인 (결제 후 장바구니 없는 상황 방지)
        List<Cart> cartItems = cartRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다. 결제를 진행할 수 없습니다.");
        }

        // Toss Payments API 호출
        try {
            WebClient.create("https://api.tosspayments.com/v1/payments/confirm")
                    .post()
                    .header("Authorization", "Basic " +
                            Base64.getEncoder().encodeToString((secretKey + ":").getBytes()))
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("결제 승인 요청 실패: Toss Payments API 오류", e);
        }

        // Payment 저장
        Payment payment = Payment.builder()
                .user(user)
                .paymentKey(request.getPaymentKey())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 장바구니 아이템을 주문 내역으로 이동 (이미 위에서 조회했으므로 재사용)
        int totalOrderAmount = 0;

        // History 저장
        for (Cart cart : cartItems) {

            int orderAmount = cart.getProduct().getPrice() * cart.getCount();
            totalOrderAmount += orderAmount;

            historyRepository.save(
                    History.builder()
                            .user(user)
                            .product(cart.getProduct())
                            .count(cart.getCount())
                            .price(cart.getProduct().getPrice())
                            .totalPrice(orderAmount)
                            .createdAt(LocalDateTime.now())
                            .status("PAID")
                            .build()
            );
        }

        // ⭐ History 저장 후 flush하여 DB에 반영 (membershipUpgrade가 조회할 수 있도록)
        entityManager.flush();

        // ⭐⭐⭐ for문 끝나고 딱 1번 호출해야 함!!
        membershipService.membershipUpgrade(user.getId(), totalOrderAmount);

        // 장바구니 비우기
        cartRepository.deleteByUserId(user.getId());
    }
}