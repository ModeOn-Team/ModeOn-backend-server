package com.modeon.backend.history.controller;

import com.modeon.backend.entity.User;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryRepository historyRepository;

    @GetMapping
    public List<History> getUserHistory(@AuthenticationPrincipal User user) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

    }
}
