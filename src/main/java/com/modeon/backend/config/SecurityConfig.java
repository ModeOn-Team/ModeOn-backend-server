package com.modeon.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // [테스트용 임시 설정] /api/users/1 경로에 대한 인증 요구사항을 해제
                        .requestMatchers("/api/**").permitAll()

                        // 모든 다른 요청은 인증된 사용자에게만 허용
                        .anyRequest().authenticated()
                );

        // API 테스트를 위해 폼 로그인 기능과 HTTP Basic 인증을 잠시 비활성화 (선택 사항)
        // 만약 로그인 페이지가 계속 뜨면 주석을 해제해 보세요.
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}