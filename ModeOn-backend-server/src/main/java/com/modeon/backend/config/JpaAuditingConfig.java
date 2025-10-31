package com.modeon.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing 활성화를 위한 설정 클래스
    // ChatRoom, ChatMessage 엔티티의 @CreatedDate, @LastModifiedDate를 위해 필요
}

