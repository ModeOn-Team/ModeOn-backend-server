package com.modeon.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image_url", columnDefinition = "Text")
    private String profileImageUrl;

    private String address;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private boolean enabled;

    @PrePersist
    protected void onCreate() {
        if (this.role == null || this.role.isBlank()) {
            this.role = "ROLE_USER";
        }

        // enabled가 명시적으로 설정되지 않은 경우에만 role 기반으로 설정
        if (!this.enabled) {
            this.enabled = this.role.equals("ROLE_ADMIN");
        }
    }

    @Column(nullable = false)
    @Builder.Default
    private String role = "ROLE_USER";

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isEnabled() {return enabled; }

    // 소셜 프로바이더
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership", nullable = false)
    @Builder.Default
    private MembershipLevel membership = MembershipLevel.WELCOME;

    @Column(name = "point")
    @Builder.Default
    private Integer point = 0;

    @Column(name = "birthmonth")
    private LocalDate birthMonth;

    // 주문 내역
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}