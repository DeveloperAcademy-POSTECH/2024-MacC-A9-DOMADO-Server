package com.onemorethink.domadosever.global.security.jwt;


import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import com.onemorethink.domadosever.global.security.entity.RefreshToken;
import com.onemorethink.domadosever.global.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private SecretKey secretKey;


    @PostConstruct
    protected void init() {
        String secret = jwtProperties.getSecret();
        // Secret key는 Base64로 인코딩된 문자열이어야 함
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String createAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getAccessToken().getValidity());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("auth", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(Authentication authentication, String userAgent, String clientIp) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getRefreshToken().getValidity());

        String refreshToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();

        saveRefreshToken(userDetails.getUsername(), refreshToken, userAgent, clientIp, validity);

        return refreshToken;
    }

    // Refresh Token을 DB에 저장
    private void saveRefreshToken(String username, String token, String userAgent,
                                  String clientIp, Date validity) {
        com.onemorethink.domadosever.domain.user.entity.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .userAgent(userAgent)
                .clientIp(clientIp)
                .expiryDate(validity.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    // Token에서 Authentication 추출
    public Authentication getAuthentication(String token) {
        Claims claims = extractAllClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // Token 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    // Claims 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Token에서 username(subject) 추출
    public String getUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Token 만료 시간 추출
    public Date getExpirationDate(String token) {
        return extractAllClaims(token).getExpiration();
    }
}
