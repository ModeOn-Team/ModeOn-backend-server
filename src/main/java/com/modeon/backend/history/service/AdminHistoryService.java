package com.modeon.backend.history.service;



import com.modeon.backend.history.dto.UpdateDeliveryStatusRequest;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        // 상태 변화에 따라 시간 기록
        if ("SHIPPING".equals(req.getStatus())) {
            history.setShippedAt(LocalDateTime.now());
        }

        if ("DELIVERED".equals(req.getStatus())) {
            history.setDeliveredAt(LocalDateTime.now());
        }


        historyRepository.save(history);
    }
}
