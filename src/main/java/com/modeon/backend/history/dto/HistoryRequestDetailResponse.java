package com.modeon.backend.history.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HistoryRequestDetailResponse {

    private Long id;

    private String productName;

    private String username;

    private String requestStatus;

    private String requestReason;

    private String createdAt;

    private List<String> requestImages;

    private String adminResponseReason;
}
