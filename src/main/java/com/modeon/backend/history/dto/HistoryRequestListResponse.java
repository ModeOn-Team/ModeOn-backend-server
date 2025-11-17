package com.modeon.backend.history.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryRequestListResponse {
    private Long id;
    private String productName;
    private String username;
    private String requestStatus;
    private String requestReason;
    private String createdAt;
}
