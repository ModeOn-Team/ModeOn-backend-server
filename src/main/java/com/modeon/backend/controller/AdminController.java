package com.modeon.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/${ADMIN_URL}")
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/Health")
    public String HealthCheck(){
        return "잘된다";
    }
}
