package com.modeon.backend.history.controller;
import com.modeon.backend.history.dto.AdminDecisionRequest;
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
    @GetMapping("/requests")
    public ResponseEntity<?> getRequests() {
        return ResponseEntity.ok(adminHistoryService.getRequestList());
    }

    // PATCH /api/admin/history/{id}/status
    @PatchMapping("/{historyId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long historyId,
            @RequestBody UpdateDeliveryStatusRequest req
    ) {
        adminHistoryService.updateStatus(historyId, req);
        return ResponseEntity.ok("배송 상태가 변경되었습니다.");
    }

    // 환불 승인
    @PatchMapping("/{historyId}/refund/approve")
    public ResponseEntity<?> approveRefund(
            @PathVariable Long historyId,
            @RequestBody AdminDecisionRequest req
    ) {
        adminHistoryService.approveRefund(historyId, req);
        return ResponseEntity.ok("환불이 승인되었습니다.");
    }

    // 환불 거절
    @PatchMapping("/{historyId}/refund/reject")
    public ResponseEntity<?> rejectRefund(
            @PathVariable Long historyId,
            @RequestBody AdminDecisionRequest req
    ) {
        adminHistoryService.rejectRefund(historyId, req);
        return ResponseEntity.ok("환불이 거절되었습니다.");
    }

    // 교환 승인
    @PatchMapping("/{historyId}/exchange/approve")
    public ResponseEntity<?> approveExchange(
            @PathVariable Long historyId,
            @RequestBody AdminDecisionRequest req
    ) {
        adminHistoryService.approveExchange(historyId, req);
        return ResponseEntity.ok("교환이 승인되었습니다.");
    }

    // 교환 거절
    @PatchMapping("/{historyId}/exchange/reject")
    public ResponseEntity<?> rejectExchange(
            @PathVariable Long historyId,
            @RequestBody AdminDecisionRequest req
    ) {
        adminHistoryService.rejectExchange(historyId, req);
        return ResponseEntity.ok("교환이 거절되었습니다.");
    }
    }
