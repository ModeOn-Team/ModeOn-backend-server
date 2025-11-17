package com.modeon.backend.history.service;

import com.modeon.backend.history.dto.AdminDecisionRequest;
import com.modeon.backend.history.dto.HistoryRequestListResponse;
import com.modeon.backend.history.dto.UpdateDeliveryStatusRequest;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHistoryService {

    private final HistoryRepository historyRepository;
    private final AuthenticationService authenticationService;

    public void updateStatus(Long historyId, UpdateDeliveryStatusRequest req) {
        authenticationService.checkAdmin();

        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("해당 주문내역을 찾을 수 없습니다."));

        history.setStatus(req.getStatus());
        history.setTrackingNumber(req.getTrackingNumber());
        history.setCourierCode(req.getCourierCode());

        if ("SHIPPING".equals(req.getStatus())) {
            history.setShippedAt(LocalDateTime.now());
        }

        if ("DELIVERED".equals(req.getStatus())) {
            history.setDeliveredAt(LocalDateTime.now());
        }

        historyRepository.save(history);
    }

    // 환불 승인
    public void approveRefund(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();

        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("해당 주문내역을 찾을 수 없습니다."));

        h.setRequestStatus("REFUND_APPROVED");
        h.setAdminResponseReason(req.getReason());  // 관리자 사유 저장
        historyRepository.save(h);
    }

    // 환불 거절
    public void rejectRefund(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();

        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("해당 주문내역을 찾을 수 없습니다."));

        h.setRequestStatus("REFUND_REJECTED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    // 교환 승인
    public void approveExchange(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();

        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("해당 주문내역을 찾을 수 없습니다."));

        h.setRequestStatus("EXCHANGE_APPROVED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    //교환 거절
    public void rejectExchange(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();

        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("해당 주문내역을 찾을 수 없습니다."));

        h.setRequestStatus("EXCHANGE_REJECTED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    public List<HistoryRequestListResponse> getRequestList() {
        authenticationService.checkAdmin();

        List<String> statuses = List.of("REFUND_REQUEST", "EXCHANGE_REQUEST");

        return historyRepository.findByRequestStatusIn(statuses)
                .stream()
                .map(h -> HistoryRequestListResponse.builder()
                        .id(h.getId())
                        .productName(h.getProduct().getName())
                        .username(h.getUser().getUsername())
                        .requestStatus(h.getRequestStatus())
                        .requestReason(h.getRequestReason()) // 사용자 사유
                        .createdAt(h.getCreatedAt().toString())
                        .build()
                )
                .toList();
    }
}
