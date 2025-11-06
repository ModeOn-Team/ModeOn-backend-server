package com.modeon.backend.payment.service;

import com.modeon.backend.cart.entity.Cart;
import com.modeon.backend.cart.repository.CartRepository;
import com.modeon.backend.entity.User;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.payment.dto.PaymentConfirmRequest;
import com.modeon.backend.payment.entity.Payment;
import com.modeon.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final HistoryRepository historyRepository;


    @Value("${toss.secret-key}")
    private String secretKey;

    public void confirmPayment(User user, PaymentConfirmRequest request) {

        // 서버에 승인 요청 보내야함
        WebClient.create("https://api.tosspayments.com/v1/payments/confirm")
                .post()
                .header("Authorization", "Basic " +
                        Base64.getEncoder().encodeToString((secretKey + ":").getBytes()))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //  Payment 저장
        Payment payment = Payment.builder()
                .user(user)
                .paymentKey(request.getPaymentKey())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 장바구니 가져오기
        List<Cart> cartItems = cartRepository.findByUserId(user.getId());

        // 주문 내역(History)로 옮기기
        for (Cart cart : cartItems) {
            historyRepository.save(
                    History.builder()
                            .user(user)
                            .product(cart.getProduct())
                            .count(cart.getCount())
                            .price(cart.getProduct().getPrice())
                            .totalPrice(cart.getProduct().getPrice() * cart.getCount())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }

        // 장바구니 비우기
        cartRepository.deleteByUserId(user.getId());
    }
}
