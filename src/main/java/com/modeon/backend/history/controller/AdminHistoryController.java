package com.modeon.backend.history.controller;
import com.modeon.backend.history.dto.UpdateDeliveryStatusRequest;
import com.modeon.backend.history.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/history")
public class AdminHistoryController {

    private final AdminHistoryService adminHistoryService;

    // PATCH /api/admin/history/{id}/status
    @PatchMapping("/{historyId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long historyId,
            @RequestBody UpdateDeliveryStatusRequest req
    ) {
        adminHistoryService.updateStatus(historyId, req);
        return ResponseEntity.ok("배송 상태가 변경되었습니다.");
    }
}
