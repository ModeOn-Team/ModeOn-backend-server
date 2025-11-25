package com.modeon.backend.payment.repository;

import com.modeon.backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 중복 결제 방지를 위한 orderId 존재 여부 확인
    boolean existsByOrderId(String orderId);
}