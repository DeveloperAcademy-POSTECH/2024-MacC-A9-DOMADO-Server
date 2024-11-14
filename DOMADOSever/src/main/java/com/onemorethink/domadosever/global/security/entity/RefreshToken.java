package com.onemorethink.domadosever.global.security.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token"),
        @Index(name = "idx_refresh_token_user", columnList = "user_id"),
        @Index(name = "idx_refresh_token_expiry", columnList = "expiry_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private String clientIp;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private LocalDateTime revokedDate;

    @Builder
    public RefreshToken(String token, User user, String userAgent, String clientIp, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.userAgent = userAgent;
        this.clientIp = clientIp;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public boolean isRevoked() {
        return this.revokedDate != null;
    }

    public void revoke() {
        this.revokedDate = LocalDateTime.now();
    }
}
