package com.modeon.backend.config;

import com.modeon.backend.entity.User;
import com.modeon.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            boolean adminExists = userRepository.existsByRole("ROLE_ADMIN");

            if (!adminExists) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ROLE_ADMIN");
                admin.setEnabled(true);

                userRepository.save(admin);
                System.out.println("✅ 기본 관리자 계정 생성됨: " + adminUsername);
            } else {
                System.out.println("ℹ️ 관리자 계정이 이미 존재합니다. 초기화 생략.");
            }
        };
    }
}