package com.modeon.backend.controller;

import com.modeon.backend.service.NaverAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NaverAuthController {

    private final NaverAuthService naverAuthService;

    @PostMapping("/api/naver-auth/token")
    public String getToken() {
        return naverAuthService.requestAccessToken();
    }
}

