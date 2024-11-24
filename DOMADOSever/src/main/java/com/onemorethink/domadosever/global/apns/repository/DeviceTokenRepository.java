package com.onemorethink.domadosever.global.apns.repository;

import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.apns.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserAndActiveIsTrue(User user);
    Optional<DeviceToken> findByTokenAndActiveIsTrue(String token);
    boolean existsByTokenAndActiveIsTrue(String token);
}
