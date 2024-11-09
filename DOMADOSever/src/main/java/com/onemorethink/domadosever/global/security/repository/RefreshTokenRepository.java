package com.onemorethink.domadosever.global.security.repository;


import com.onemorethink.domadosever.global.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Query("""
        SELECT r FROM RefreshToken r 
        WHERE r.user.id = :userId 
        AND r.expiryDate > :now 
        AND r.revokedDate IS NULL
    """)
    List<RefreshToken> findAllValidTokensByUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // 선택적: 만료된 토큰 삭제를 위한 메서드
    @Modifying
    @Query("""
        DELETE FROM RefreshToken r 
        WHERE r.expiryDate <= :now 
        OR r.revokedDate IS NOT NULL
    """)
    void deleteAllExpiredTokens(@Param("now") LocalDateTime now);

    // 선택적: 특정 사용자의 모든 토큰 조회
    @Query("SELECT r FROM RefreshToken r WHERE r.user.id = :userId")
    List<RefreshToken> findAllByUserId(@Param("userId") Long userId);
}