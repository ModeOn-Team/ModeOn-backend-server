package com.modeon.backend.history.service;

import com.modeon.backend.history.dto.AdminDecisionRequest;
import com.modeon.backend.history.dto.HistoryRequestDetailResponse;
import com.modeon.backend.history.dto.HistoryRequestListResponse;
import com.modeon.backend.history.dto.UpdateDeliveryStatusRequest;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHistoryService {

    private final HistoryRepository historyRepository;
    private final AuthenticationService authenticationService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    // 요청 상세 조회
    public HistoryRequestDetailResponse getRequestDetail(Long historyId) {
        authenticationService.checkAdmin();

        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("요청을 찾을 수 없습니다."));

        return HistoryRequestDetailResponse.builder()
                .id(h.getId())
                .productName(h.getProduct().getName())
                .username(h.getUser().getUsername())
                .requestStatus(h.getRequestStatus())
                .requestReason(h.getRequestReason())
                .createdAt(h.getCreatedAt().format(dtf))
                .requestImages(h.getRequestImages())
                .adminResponseReason(h.getAdminResponseReason())
                .build();
    }

    // 배송 상태 변경
    public void updateStatus(Long historyId, UpdateDeliveryStatusRequest req) {
        authenticationService.checkAdmin();

        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("주문내역을 찾을 수 없습니다."));

        h.setStatus(req.getStatus());
        h.setTrackingNumber(req.getTrackingNumber());
        h.setCourierCode(req.getCourierCode());

        if ("SHIPPING".equals(req.getStatus())) {
            h.setShippedAt(LocalDateTime.now());
        }

        if ("DELIVERED".equals(req.getStatus())) {
            h.setDeliveredAt(LocalDateTime.now());
        }

        historyRepository.save(h);
    }

    public void approveRefund(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();
        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("주문내역을 찾을 수 없습니다."));
        h.setRequestStatus("REFUND_APPROVED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    public void rejectRefund(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();
        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("주문내역을 찾을 수 없습니다."));
        h.setRequestStatus("REFUND_REJECTED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    public void approveExchange(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();
        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("주문내역을 찾을 수 없습니다."));
        h.setRequestStatus("EXCHANGE_APPROVED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    public void rejectExchange(Long historyId, AdminDecisionRequest req) {
        authenticationService.checkAdmin();
        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("주문내역을 찾을 수 없습니다."));
        h.setRequestStatus("EXCHANGE_REJECTED");
        h.setAdminResponseReason(req.getReason());
        historyRepository.save(h);
    }

    // 요청 목록
    public List<HistoryRequestListResponse> getRequestList() {
        authenticationService.checkAdmin();

        List<String> statuses = List.of("REFUND_REQUEST", "EXCHANGE_REQUEST");

        return historyRepository.findByRequestStatusIn(statuses)
                .stream()
                .map(h ->
                        HistoryRequestListResponse.builder()
                                .id(h.getId())
                                .productName(h.getProduct().getName())
                                .username(h.getUser().getUsername())
                                .requestStatus(h.getRequestStatus())
                                .requestReason(h.getRequestReason())
                                .createdAt(h.getCreatedAt().format(dtf))
                                .build()
                )
                .toList();
    }
}
