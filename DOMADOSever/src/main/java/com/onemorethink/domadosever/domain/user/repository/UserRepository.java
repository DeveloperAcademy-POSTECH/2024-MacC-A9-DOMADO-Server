package com.onemorethink.domadosever.domain.user.repository;

import com.onemorethink.domadosever.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // email 이용한 사용자 조회
    Optional<User> findByEmail(String email);

    // 주어진 email 이미 회원가입한 사용자 있는지 확인
    boolean existsByEmail(String email);
}
